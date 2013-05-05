/**
 * Copyright (c) 2005, European Commission project OneLab under contract 034819
 * (http://www.one-lab.org) All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met: - Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. - Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. -
 * Neither the name of the University Catholique de Louvain - UCL nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission. THIS SOFTWARE IS
 * PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.git.original.common.bloom;

import com.git.original.common.utils.ByteUtils;
import com.git.original.common.utils.Md5Utils;

/**
 * 基于Hadoop的BloomFilter实现类修改:
 * <p>
 * <li>内部使用Lucene的OpenBitSet, 替代Java的BitSet类<br>
 * <li>限定hash函数数量为8<br>
 * <li>为简化Hmail系统内的使用, 添加一些辅助方法
 * 
 * @author linaoxiang
 */
public class BloomFilter extends Filter {
	/** The bit vector. */
	OpenBitSet bits;

	/**
	 * Constructor
	 * 
	 * @param numBits
	 *            The bits count of <i>this</i> filter.
	 */
	public BloomFilter(int numBits) {
		super(numBits, 8);
		bits = new OpenBitSet(this.numBits);
	}

	/**
	 * @param bits
	 */
	public BloomFilter(long[] bits) {
		super(bits.length << 6, 8);
		this.bits = new OpenBitSet(bits, bits.length);
	}

	public BloomFilter(byte[] bits, int offset, int length) {
		super(length << 3, 8);

		if ((length & 0x7) != 0) {
			throw new IllegalArgumentException(
					"bits length must be dividable by 8");
		}

		long[] array = new long[length >>> 3];
		for (int i = 0, j = offset, len = array.length; i < len; i++, j += 8) {
			array[i] = ByteUtils.toLong(bits, j);
		}

		this.bits = new OpenBitSet(array, array.length);
	}

	public BloomFilter(OpenBitSet bits) {
		super(bits.getNumWords() << 6, 8);
		this.bits = bits;
	}

	@Override
	public void add(String key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}

		int[] h = hash(key);
		for (int i = 0; i < nbHash; i++) {
			bits.fastSet(h[i]);
		}
	}

	public void add(byte[] md5Key) {
		if (md5Key == null) {
			throw new NullPointerException("key cannot be null");
		}

		int[] h = hash(md5Key);
		for (int i = 0; i < nbHash; i++) {
			bits.fastSet(h[i]);
		}
	}

	@Override
	public void and(Filter filter) {
		if (filter == null || !(filter instanceof BloomFilter)
				|| filter.numBits != this.numBits
				|| filter.nbHash != this.nbHash) {
			throw new IllegalArgumentException("filters cannot be and-ed");
		}

		this.bits.and(((BloomFilter) filter).bits);
	}

	@Override
	public boolean membershipTest(String key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}

		int[] h = hash(key);
		for (int i = 0; i < nbHash; i++) {
			if (!bits.fastGet(h[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 直接使用MD5值判断Key是否属于本过滤器
	 * 
	 * @param md5Key
	 *            Key的MD5值
	 * @return true=属于本过滤器; false=不属于本过滤器
	 */
	public boolean membershipTest(byte[] md5Key) {
		if (md5Key == null) {
			throw new NullPointerException("key cannot be null");
		}
		int[] h = hash(md5Key);
		for (int i = 0; i < nbHash; i++) {
			if (!bits.fastGet(h[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void not() {
		bits.flip(0, numBits - 1);
	}

	@Override
	public void or(Filter filter) {
		if (filter == null || !(filter instanceof BloomFilter)
				|| filter.numBits != this.numBits
				|| filter.nbHash != this.nbHash) {
			throw new IllegalArgumentException("filters cannot be or-ed");
		}
		bits.or(((BloomFilter) filter).bits);
	}

	@Override
	public void xor(Filter filter) {
		if (filter == null || !(filter instanceof BloomFilter)
				|| filter.numBits != this.numBits
				|| filter.nbHash != this.nbHash) {
			throw new IllegalArgumentException("filters cannot be xor-ed");
		}
		bits.xor(((BloomFilter) filter).bits);
	}

	@Override
	public String toString() {
		return bits.toString();
	}

	/**
	 * @return size of the the bloomfilter
	 */
	public int getBitSize() {
		return this.numBits;
	}

	/**
	 * Expert: returns the long[] storing the bits
	 */
	public long[] getBits() {
		return this.bits.getBits();
	}

	/**
	 * Expert: returns the byte[] storing the bits
	 */
	public OpenBitSet getBitAsObject() {
		return this.bits;
	}

	private final int[] hash(String key) {
		return hash(Md5Utils.md5(key));
	}

	private final int[] hash(byte[] md5Key) {
		int[] h = new int[nbHash]; // 注意: 固定nbHash为8
		for (int i = 0, initval = 0; i < nbHash; i++) {
			initval = ((md5Key[i << 1] & 0xFF) << 8 | (md5Key[(i << 1) + 1] & 0xFF));
			h[i] = initval % this.numBits;
		}
		return h;
	}

} // end class
