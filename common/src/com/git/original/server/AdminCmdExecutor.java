package com.git.original.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.common.utils.Pair;
import com.git.original.server.annotation.AdminCmdDescription;

/**
 * 只支持以下三种种格式的方法参数:
 * <ol>
 * <li>xxx()
 * <li>xxx(String)
 * <li>xxx(String, {@link #getAttachedParamClazz()})
 * </ol>
 */
public class AdminCmdExecutor {
    /**
     * 管理指令(小写字符串) --> 指令别名
     */
    private final Map<String, CommandEntry> cmd2Entry = new TreeMap<String, CommandEntry>();

    /**
     * 附加参数的类型
     */
    private final Class<?> attachedParamClazz;

    /**
     * 日志输出对象
     */
    private Logger logger;

    /** 缓存 */
    private static Map<Class<?>, Map<String, CommandEntry>> cachedCmdEntry;
    static {
        cachedCmdEntry = new ConcurrentHashMap<Class<?>, Map<String, CommandEntry>>();
    }

    public AdminCmdExecutor() {
        this(null, LoggerFactory.getLogger(AdminCmdExecutor.class));
    }

    public AdminCmdExecutor(Class<?> attachedParamClazz, Logger logger) {
        this.attachedParamClazz = attachedParamClazz;
        this.logger = logger;
    }

    /**
     * @return the cmd2Entry
     */
    public Map<String, CommandEntry> getCmd2Entry() {
        return Collections.unmodifiableMap(cmd2Entry);
    }

    /**
     * @return the attachedParamClazz
     */
    public Class<?> getAttachedParamClazz() {
        return attachedParamClazz;
    }

    /**
     * 解析带标注方法的类, 并自动注册指令
     */
    public final void analysisCmdAnnotation(Object target) {
        Class<?> clazz = target.getClass();

        Map<String, CommandEntry> map = cachedCmdEntry.get(clazz);
        if (map == null) { // 没能命中缓存
            map = new HashMap<String, CommandEntry>();
            while (clazz != null) {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method: methods) {
                    if (!method.isAnnotationPresent(AdminCmdDescription.class)) {
                        continue;
                    }

                    AdminCmdDescription desc = method
                        .getAnnotation(AdminCmdDescription.class);
                    CommandEntry cmdEntry = createCmdEntryFromMethod(method,
                        desc); // 必须是空白target,因为需要放入缓存
                    if (cmdEntry != null) {
                        this.addCmdEntry(cmdEntry, map, true);
                    }
                }

                // 递归查找父类方法
                clazz = clazz.getSuperclass();
            }

            // 添加到缓存
            cachedCmdEntry.put(target.getClass(), map);
        }

        if (map.isEmpty()) {
            return;
        }

        Map<String, CommandEntry> tmp = new HashMap<String, AdminCmdExecutor.CommandEntry>();
        for (CommandEntry cached: map.values()) {
            // 必须重新构建CommandEntry对象，否则会影响到cache中的CommandEntry
            CommandEntry entry = new CommandEntry(cached);
            entry.fillAllMethodTarget(target);
            tmp.put(entry.getCommand(), entry);
        }

        // 整合到cmd2Entry
        synchronized (this) {
            this.mergeCmd2Entry(tmp, this.cmd2Entry);
        }
    }

    /**
     * 执行指令
     * 
     * @param message
     * @param attachedParam
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public String invokeCommand(String message, Object attachedParam)
        throws IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
        String[] infos = message.split("\\s+", 3);

        Pair<Method, Object> methodPair = null;
        String strParam = null;
        CommandEntry cmdEntry = this.cmd2Entry.get(infos[0].toLowerCase());
        if (cmdEntry != null) {
            if (infos.length > 1) {
                methodPair = cmdEntry.getMethodByParam(infos[1]);
                if (infos.length > 2) {
                    strParam = infos[2];
                }
            }
            if (methodPair == null) {
                methodPair = cmdEntry.getMethodByParam(StringUtils.EMPTY);
                strParam = message.substring(infos[0].length()).trim();
            }
        }

        if (methodPair == null) {
            // 未知指令
            return ("unknown command: " + message);
        } else {
            Method method = methodPair.getFirst();
            String resultDesc = null;

            /*
             * 只支持两种格式的方法参数
             */
            Object[] args = new Object[method.getParameterTypes().length];
            if (args.length > 0) {
                args[0] = strParam;
                if (args.length > 1) {
                    args[1] = attachedParam;
                }
            }

            if (method.isSynthetic()) {
                synchronized (this) {
                    // 执行操作
                    resultDesc = (String) method.invoke(methodPair.getSecond(),
                        args);
                }
            } else {
                // 执行操作
                resultDesc = (String) method.invoke(methodPair.getSecond(),
                    args);
            }

            return resultDesc;
        }
    }

    /**
     * 注册指定管理指令对应的管理方法
     * 
     * @param method
     *            管理方法(null=撤销指定指令的管理方法)
     * @param cmdDesc
     *            指令描述
     * @throws NullPointerException
     *             管理指令参数不能为NULL
     */
    private synchronized final CommandEntry createCmdEntryFromMethod(
        Method method, AdminCmdDescription cmdDesc) {
        if (cmdDesc == null || StringUtils.isEmpty(cmdDesc.cmd())) {
            throw new NullPointerException("command is null!");
        }
        String cmd = cmdDesc.cmd().toLowerCase().trim();

        Class<?>[] pts = method.getParameterTypes();
        if (pts.length >= 3) {
            throw new IllegalArgumentException(
                "method's parameter count must be less then 3: name="
                    + method.getName());
        }

        if (pts.length > 0) {
            // 检查参数是否为String类型
            pts[0].asSubclass(String.class);
            if (pts.length > 1) {
                // 检查参数是否为attachedParamClazz类型
                if (attachedParamClazz == null) {
                    throw new IllegalArgumentException(
                        "method must have only one parameters: name="
                            + method.getName());
                } else {
                    pts[1].asSubclass(attachedParamClazz);
                }
            }
        }

        CommandEntry cmdEntry = new CommandEntry(cmd);

        String[] aliases = cmdDesc.alias();
        if (aliases != null && aliases.length > 0) {
            for (String alias: aliases) {
                cmdEntry.addAlias(alias);
            }
        }

        boolean registered = false;
        method.setAccessible(true);
        String[] params = cmdDesc.param();
        if (params != null && params.length > 0) {
            for (String param: params) {
                registered |= cmdEntry.putMethod(param, method, null);
            }
        } else {
            registered |= cmdEntry.putMethod(StringUtils.EMPTY, method, null);
        }

        // ---- 附加信息 -----
        String[] infos = cmdDesc.summary();
        if (infos != null && infos.length > 0) {
            for (String info: infos) {
                cmdEntry.addSummary(info);
            }
        }
        infos = cmdDesc.usage();
        if (infos != null && infos.length > 0) {
            for (String info: infos) {
                cmdEntry.addUsage(info);
            }
        }
        infos = cmdDesc.argDesc();
        if (infos != null && infos.length > 0) {
            for (String info: infos) {
                cmdEntry.addArgDesc(info);
            }
        }

        return cmdEntry;
    }

    private void mergeCmd2Entry(Map<String, CommandEntry> src,
        Map<String, CommandEntry> dest) {
        if (dest == null) {
            throw new NullPointerException("destination is null!");
        }

        // 整合到cmd2Entry
        for (Entry<String, CommandEntry> entry: src.entrySet()) {
            if (!entry.getKey().equals(entry.getValue().getCommand())) {
                // 略过别名关联关系, 避免重复添加
                continue;
            }

            addCmdEntry(entry.getValue(), dest, false);
        }
    }

    private void addCmdEntry(CommandEntry cmdEntry,
        Map<String, CommandEntry> destMap, boolean ignoreAlias) {
        CommandEntry destEntry = destMap.get(cmdEntry.getCommand());
        if (destEntry == null) {
            destMap.put(cmdEntry.getCommand(), cmdEntry);

            if (!ignoreAlias && cmdEntry.getAliasSet() != null) {
                for (String alias: cmdEntry.getAliasSet()) {
                    CommandEntry old = destMap.get(alias);
                    if (old != null && old != cmdEntry) {
                        // 已存在该别名对应的指令
                        logger.warn("Ignore Duplicated Command: {}", alias);
                        continue;
                    } else {
                        destMap.put(alias, cmdEntry);
                    }
                }
            }

            return;
        }

        if (!ignoreAlias && cmdEntry.getAliasSet() != null) {
            for (String alias: cmdEntry.getAliasSet()) {
                CommandEntry old = destMap.get(alias);
                if (old != null && old != destEntry) {
                    // 已存在该别名对应的指令
                    logger.warn("Ignore Duplicated Command: {}", alias);
                    continue;
                } else {
                    destMap.put(alias, destEntry);
                }

                destEntry.addAlias(alias);
            }
        }

        for (Entry<String, Pair<Method, Object>> entry: cmdEntry.param2Method
            .entrySet()) {
            destEntry.putMethod(entry.getKey(), entry.getValue().getFirst(),
                entry.getValue().getSecond());
        }

        // ---- 附加信息 -----
        if (cmdEntry.getSummary() != null) {
            destEntry.addSummary(cmdEntry.getSummary());
        }
        if (cmdEntry.getUsage() != null) {
            destEntry.addUsage(cmdEntry.getUsage());
        }
        if (cmdEntry.getArgDesc() != null) {
            destEntry.addArgDesc(cmdEntry.getArgDesc());
        }
    }

    /**
     * 管理指令条目
     * 
     * @author <a href="mailto:qiusheng@corp.netease.com">QiuSheng</a>
     */
    public static class CommandEntry {
        /** 主指令 */
        private final String cmd;

        /** 别名列表 */
        private Set<String> aliasSet;

        /** 固定参数 --> 执行方法 映射表 (无参数的指令,默认使用 "" 空白字符串作为参数) */
        private Map<String, Pair<Method, Object>> param2Method;

        /** 指令概述 */
        private StringBuilder summary;

        /** 指令使用帮助 */
        private StringBuilder usage;

        /** 指令参数说明 */
        private StringBuilder argDesc;

        public CommandEntry(String cmd) {
            if (StringUtils.isEmpty(cmd)) {
                throw new NullPointerException("command is null!");
            }
            this.cmd = cmd;
            this.param2Method = new HashMap<String, Pair<Method, Object>>();
        }

        public CommandEntry(CommandEntry other) {
            if (other == null) {
                throw new NullPointerException("commond-entry is null!");
            }
            this.cmd = other.cmd;
            this.param2Method = new HashMap<String, Pair<Method, Object>>();
            for (Entry<String, Pair<Method, Object>> entry: other.param2Method
                .entrySet()) {
                Pair<Method, Object> pair = entry.getValue();
                this.param2Method.put(entry.getKey(), new Pair<Method, Object>(
                    pair.getFirst(), pair.getSecond()));
            }

            if (other.aliasSet != null) {
                this.aliasSet = new HashSet<String>(other.aliasSet);
            }

            if (other.summary != null) {
                this.summary = new StringBuilder(other.summary);
            }
            if (other.usage != null) {
                this.usage = new StringBuilder(other.usage);
            }
            if (other.argDesc != null) {
                this.argDesc = new StringBuilder(other.argDesc);
            }
        }

        void fillAllMethodTarget(Object target) {
            for (Pair<Method, Object> pair: param2Method.values()) {
                pair.setSecond(target);
            }
        }

        /**
         * @param string
         * @return
         */
        public Pair<Method, Object> getMethodByParam(String param) {
            return param2Method.get(param.toLowerCase().trim());
        }

        /**
         * @return
         */
        public String getCommand() {
            return cmd;
        }

        /**
         * @return the aliasSet
         */
        public Set<String> getAliasSet() {
            return aliasSet;
        }

        /**
         * @return the usage
         */
        public StringBuilder getUsage() {
            return usage;
        }

        /**
         * @param usage
         */
        public void addUsage(String usage) {
            if (StringUtils.isEmpty(usage)) {
                return;
            }

            if (this.usage == null) {
                this.usage = new StringBuilder();
            }

            this.usage.append('\t').append(usage).append("\r\n");
        }

        /**
         * @param param
         * @param method
         * @return
         */
        public boolean putMethod(String param, Method method, Object target) {
            if (param == null) {
                param = StringUtils.EMPTY;
            } else {
                param = param.toLowerCase().trim();
            }

            if (this.param2Method.containsKey(param)) {
                // 不允许被覆盖
                return false;
            }

            this.param2Method.put(param, new Pair<Method, Object>(method,
                target));
            return true;
        }

        /**
         * @param alias
         */
        public void addAlias(String alias) {
            if (alias == null)
                return;
            if (aliasSet == null) {
                aliasSet = new HashSet<String>();
            }
            aliasSet.add(alias.toLowerCase().trim());
        }

        /**
         * @return the summary
         */
        public StringBuilder getSummary() {
            return summary;
        }

        /**
         * @return the argDesc
         */
        public StringBuilder getArgDesc() {
            return argDesc;
        }

        /**
         * @param argDesc
         */
        public void addArgDesc(String argDesc) {
            if (StringUtils.isEmpty(argDesc)) {
                return;
            }

            if (this.argDesc == null) {
                this.argDesc = new StringBuilder();
            }

            this.argDesc.append('\t').append(argDesc).append("\r\n");
        }

        /**
         * @param summary
         */
        public void addSummary(String summary) {
            if (StringUtils.isEmpty(summary)) {
                return;
            }

            if (this.summary == null) {
                this.summary = new StringBuilder();
            }

            this.summary.append('\t').append(summary).append("\r\n");
        }

        public void addSummary(StringBuilder sb) {
            if (sb == null)
                return;

            if (this.summary == null) {
                this.summary = new StringBuilder();
            }

            this.summary.append(sb);
        }

        public void addArgDesc(StringBuilder sb) {
            if (sb == null)
                return;

            if (this.argDesc == null) {
                this.argDesc = new StringBuilder();
            }

            this.argDesc.append(sb);
        }

        public void addUsage(StringBuilder sb) {
            if (sb == null)
                return;

            if (this.usage == null) {
                this.usage = new StringBuilder();
            }

            this.usage.append(sb);
        }

    }
}
