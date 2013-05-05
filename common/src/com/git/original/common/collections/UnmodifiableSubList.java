package com.git.original.common.collections;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * 不允许被修改,用于获取指定范围内元素的子列表类
 * 
 * @param <E>
 *            队列元素对象类型
 * 
 * @author linaoxiang
 */
public class UnmodifiableSubList<E> extends AbstractList<E> implements
		RandomAccess {

	/** 原始列表对象 */
	protected List<E> l;

	/** 起始元素在原始列表对象中偏移量 */
	protected int offset;

	/** 子列表元素总数 */
	protected int size;

	/**
	 * 构造函数
	 * 
	 * @param list
	 *            原始列表对象
	 * @param fromIndex
	 *            起始元素在原始列表对象中偏移量(include)
	 * @param toIndex
	 *            截止元素在原始列表对象中偏移量(exclude)
	 */
	public UnmodifiableSubList(List<E> list, int fromIndex, int toIndex) {
		l = list;
		this.doSetRange(fromIndex, toIndex);
	}

	/**
	 * 构造函数
	 * 
	 * @param list
	 *            原始列表对象
	 */
	public UnmodifiableSubList(List<E> list) {
		l = list;
		this.doSetRange(0, list.size());
	}

	/**
	 * 调整子列表范围
	 * <p>
	 * 注意: 不允许被继承
	 * 
	 * @param fromIndex
	 *            起始元素在原始列表对象中偏移量(include)
	 * @param toIndex
	 *            截止元素在原始列表对象中偏移量(exclude)
	 */
	protected final void doSetRange(int fromIndex, int toIndex) {
		if (fromIndex < 0) {
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		}
		if (fromIndex > toIndex) {
			throw new IllegalArgumentException("fromIndex(" + fromIndex
					+ ") > toIndex(" + toIndex + ")");
		}

		if (toIndex > l.size()) {
			toIndex = l.size();
		}

		offset = fromIndex;
		size = toIndex - fromIndex;
	}

	/**
	 * 调整子列表范围
	 * 
	 * @param fromIndex
	 *            起始元素在原始列表对象中偏移量(include)
	 * @param toIndex
	 *            截止元素在原始列表对象中偏移量(exclude)
	 */
	public void setRange(int fromIndex, int toIndex) {
		this.doSetRange(fromIndex, toIndex);
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E get(int index) {
		rangeCheck(index);
		return l.get(index + offset);
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
					+ size);
		}

		return new ListIterator<E>() {
			private ListIterator<E> i = l.listIterator(index + offset);

			public boolean hasNext() {
				return nextIndex() < size;
			}

			public E next() {
				if (hasNext()) {
					return i.next();
				} else {
					throw new NoSuchElementException();
				}
			}

			public boolean hasPrevious() {
				return previousIndex() >= 0;
			}

			public E previous() {
				if (hasPrevious()) {
					return i.previous();
				} else {
					throw new NoSuchElementException();
				}
			}

			public int nextIndex() {
				return i.nextIndex() - offset;
			}

			public int previousIndex() {
				return i.previousIndex() - offset;
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

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new UnmodifiableSubList<E>(this, fromIndex, toIndex);
	}

	/**
	 * 范围检查
	 * 
	 * @param index
	 */
	private void rangeCheck(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ",Size: "
					+ size);
		}
	}

}
