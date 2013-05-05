package com.git.original.common.collections;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * 不允许被修改,用于获取范围大小不变的子列表类
 * <p>
 * 注意: 如果指定的元素序号在子列表范围内,但超过了实际原始列表的范围，则视为虚拟元素，返回最后一个实际有效元素值
 * 
 * @param <E>
 *            队列元素对象类型
 */
public class FixedSizeUnmodifiableSubList<E> extends UnmodifiableSubList<E> {

    /**
     * 范围固定大小
     */
    private final int fixedSize;

    /**
     * 虚拟元素起始序号
     */
    private int fakeStartIndex = Integer.MAX_VALUE;

    /**
     * 构造函数
     * 
     * @param list
     * @param fromIndex
     * @param fixedSize
     */
    public FixedSizeUnmodifiableSubList(List<E> list, int fromIndex,
        int fixedSize) {
        super(list);

        if (l == null || l.isEmpty()) {
            this.fixedSize = 0;
        } else {
            this.fixedSize = fixedSize;
        }

        int toIndex = fromIndex + fixedSize;
        if (l == null) {
            toIndex = 0;
        } else if (toIndex > l.size()) {
            toIndex = l.size();
        }
        fakeStartIndex = toIndex - fromIndex;
        super.doSetRange(fromIndex, toIndex);
    }

    /**
     * 修改指定范围的起始偏移量
     * 
     * @param fromIndex
     *            起始偏移量
     */
    public void setRange(int fromIndex) {
        this.setRange(fromIndex, fromIndex + fixedSize);
    }

    @Override
    public void setRange(int fromIndex, int toIndex) {
        if (toIndex - fromIndex > fixedSize) {
            toIndex = fromIndex + fixedSize;
        }
        if (toIndex > l.size()) {
            toIndex = l.size();
        }
        fakeStartIndex = toIndex - fromIndex;

        super.setRange(fromIndex, toIndex);
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= fixedSize) {
            throw new IndexOutOfBoundsException("Index: " + index + ",Size: "
                + fixedSize);
        }

        if (index >= fakeStartIndex) {
            return super.get(fakeStartIndex - 1);
        } else {
            return super.get(index);
        }
    }

    @Override
    public int size() {
        return this.fixedSize;
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return new ListIterator<E>() {

            private int iterIndex = index;

            public boolean hasNext() {
                return nextIndex() <= fixedSize;
            }

            public E next() {
                if (hasNext()) {
                    return get(iterIndex++);
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public E previous() {
                if (hasPrevious()) {
                    return get(iterIndex--);
                } else {
                    throw new NoSuchElementException();
                }
            }

            public int nextIndex() {
                return (iterIndex + 1);
            }

            public int previousIndex() {
                return (iterIndex - 1);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void set(E e) {
                throw new UnsupportedOperationException();
            }

            public void add(E e) {
                throw new UnsupportedOperationException();
            }
        };
    }

}
