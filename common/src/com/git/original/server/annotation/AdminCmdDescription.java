/**
 *                               rights reserved. NETEASE
 *                               PROPRIETARY/CONFIDENTIAL. Use is subject to
 *                               license terms.
 */
package com.git.original.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理指令描述
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface AdminCmdDescription {
    /**
     * 管理指令, 注意: 这里不包含参数
     * 
     * @return
     */
    String cmd();

    /**
     * 指令别名
     * 
     * @return
     */
    String[] alias() default {};

    /**
     * 指令参数
     * 
     * @return
     */
    String[] param() default {};

    /**
     * 使用帮助
     * 
     * @return
     */
    String[] usage() default {};

    /**
     * 指令概述
     * 
     * @return
     */
    String[] summary() default {};

    /**
     * 指令参数说明
     * 
     * @return
     */
    String[] argDesc() default {};
}
