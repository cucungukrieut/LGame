package loon.utils;

import java.math.BigInteger;
import java.util.Arrays;

import loon.LSystem;
import loon.event.QueryEvent;

public class CharArray implements IArray {

	public static CharArray range(int start, int end) {
		CharArray array = new CharArray(end - start);
		for (int i = start; i < end; i++) {
			array.add((char) i);
		}
		return array;
	}

	public char[] items;
	public int length;
	public boolean ordered;

	public CharArray() {
		this(true, CollectionUtils.INITIAL_CAPACITY);
	}

	public CharArray(int capacity) {
		this(true, capacity);
	}

	public CharArray(boolean ordered, int capacity) {
		this.ordered = ordered;
		items = new char[capacity];
	}

	public CharArray(CharArray array) {
		this.ordered = array.ordered;
		length = array.length;
		items = new char[length];
		System.arraycopy(array.items, 0, items, 0, length);
	}

	public CharArray(char[] array) {
		this(true, array, 0, array.length);
	}

	public CharArray(boolean ordered, char[] array, int startIndex, int count) {
		this(ordered, count);
		length = count;
		System.arraycopy(array, startIndex, items, 0, count);
	}

	public void unshift(char value) {
		if (length > 0) {
			char[] items = this.items;
			char[] newItems = new char[length + 1];
			newItems[0] = value;
			System.arraycopy(items, 0, newItems, 1, length);
			this.length = newItems.length;
			this.items = newItems;
		} else {
			add(value);
		}
	}

	public void push(char value) {
		add(value);
	}

	public void add(char value) {
		char[] items = this.items;
		if (length == items.length) {
			items = relength(MathUtils.max(8, (int) (length * 1.75f)));
		}
		items[length++] = value;
	}

	public void addAll(CharArray array) {
		addAll(array, 0, array.length);
	}

	public void addAll(CharArray array, int offset, int length) {
		if (offset + length > array.length)
			throw new IllegalArgumentException(
					"offset + length must be <= length: " + offset + " + " + length + " <= " + array.length);
		addAll(array.items, offset, length);
	}

	public void addAll(char... array) {
		addAll(array, 0, array.length);
	}

	public void addAll(char[] array, int offset, int length) {
		char[] items = this.items;
		int lengthNeeded = length + length;
		if (lengthNeeded > items.length) {
			items = relength(MathUtils.max(8, (int) (lengthNeeded * 1.75f)));
		}
		System.arraycopy(array, offset, items, length, length);
		length += length;
	}

	public char get(int index) {
		if (index >= length) {
			return 0;
		}
		return items[index];
	}

	public void set(int index, char value) {
		if (index >= length) {
			int size = length;
			for (int i = size; i < index + 1; i++) {
				add(' ');
			}
			items[index] = value;
			return;
		}
		items[index] = value;
	}

	public void incr(int index, char value) {
		if (index >= length)
			throw new IndexOutOfBoundsException("index can't be >= length: " + index + " >= " + length);
		items[index] += value;
	}

	public void mul(int index, char value) {
		if (index >= length)
			throw new IndexOutOfBoundsException("index can't be >= length: " + index + " >= " + length);
		items[index] *= value;
	}

	public void insert(int index, char value) {
		if (index > length) {
			throw new IndexOutOfBoundsException("index can't be > length: " + index + " > " + length);
		}
		char[] items = this.items;
		if (length == items.length)
			items = relength(MathUtils.max(8, (int) (length * 1.75f)));
		if (ordered)
			System.arraycopy(items, index, items, index + 1, length - index);
		else
			items[length] = items[index];
		length++;
		items[index] = value;
	}

	public void swap(int first, int second) {
		if (first >= length)
			throw new IndexOutOfBoundsException("first can't be >= length: " + first + " >= " + length);
		if (second >= length)
			throw new IndexOutOfBoundsException("second can't be >= length: " + second + " >= " + length);
		char[] items = this.items;
		char firstValue = items[first];
		items[first] = items[second];
		items[second] = firstValue;
	}

	public boolean contains(char value) {
		int i = length - 1;
		char[] items = this.items;
		while (i >= 0)
			if (items[i--] == value)
				return true;
		return false;
	}

	public int indexOf(char value) {
		char[] items = this.items;
		for (int i = 0, n = length; i < n; i++)
			if (items[i] == value)
				return i;
		return -1;
	}

	public int lastIndexOf(char value) {
		char[] items = this.items;
		for (int i = length - 1; i >= 0; i--)
			if (items[i] == value)
				return i;
		return -1;
	}

	public boolean removeValue(char value) {
		char[] items = this.items;
		for (int i = 0, n = length; i < n; i++) {
			if (items[i] == value) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}

	public int removeIndex(int index) {
		if (index >= length) {
			throw new IndexOutOfBoundsException("index can't be >= length: " + index + " >= " + length);
		}
		char[] items = this.items;
		char value = items[index];
		length--;
		if (ordered) {
			System.arraycopy(items, index + 1, items, index, length - index);
		} else {
			items[index] = items[length];
		}
		return value;
	}

	public void removeRange(int start, int end) {
		if (end >= length) {
			throw new IndexOutOfBoundsException("end can't be >= length: " + end + " >= " + length);
		}
		if (start > end) {
			throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
		}
		char[] items = this.items;
		int count = end - start + 1;
		if (ordered) {
			System.arraycopy(items, start + count, items, start, length - (start + count));
		} else {
			int lastIndex = this.length - 1;
			for (int i = 0; i < count; i++)
				items[start + i] = items[lastIndex - i];
		}
		length -= count;
	}

	public boolean removeAll(CharArray array) {
		int length = this.length;
		int startlength = length;
		char[] items = this.items;
		for (int i = 0, n = array.length; i < n; i++) {
			int item = array.get(i);
			for (int ii = 0; ii < length; ii++) {
				if (item == items[ii]) {
					removeIndex(ii);
					length--;
					break;
				}
			}
		}
		return length != startlength;
	}

	public int pop() {
		return items[--length];
	}

	public int shift() {
		return removeIndex(0);
	}

	public int peek() {
		return items[length - 1];
	}

	public int first() {
		if (length == 0) {
			throw new IllegalStateException("Array is empty.");
		}
		return items[0];
	}

	@Override
	public void clear() {
		length = 0;
	}

	public char[] shrink() {
		if (items.length != length)
			relength(length);
		return items;
	}

	public char[] ensureCapacity(int additionalCapacity) {
		int lengthNeeded = length + additionalCapacity;
		if (lengthNeeded > items.length)
			relength(MathUtils.max(8, lengthNeeded));
		return items;
	}

	protected char[] relength(int newlength) {
		char[] newItems = new char[newlength];
		char[] items = this.items;
		System.arraycopy(items, 0, newItems, 0, MathUtils.min(length, newItems.length));
		this.items = newItems;
		return newItems;
	}

	public CharArray sort() {
		Arrays.sort(items, 0, length);
		return this;
	}

	public void reverse() {
		char[] items = this.items;
		for (int i = 0, lastIndex = length - 1, n = length / 2; i < n; i++) {
			int ii = lastIndex - i;
			char temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	public void shuffle() {
		char[] items = this.items;
		for (int i = length - 1; i >= 0; i--) {
			int ii = MathUtils.random(i);
			char temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	public void truncate(int newlength) {
		if (length > newlength)
			length = newlength;
	}

	public int random() {
		if (length == 0) {
			return 0;
		}
		return items[MathUtils.random(0, length - 1)];
	}

	public char[] toArray() {
		char[] array = new char[length];
		System.arraycopy(items, 0, array, 0, length);
		return array;
	}

	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (!(object instanceof CharArray))
			return false;
		CharArray array = (CharArray) object;
		int n = length;
		if (n != array.length)
			return false;
		for (int i = 0; i < n; i++)
			if (items[i] != array.items[i])
				return false;
		return true;
	}

	public String toString(String separator) {
		if (length == 0)
			return "";
		char[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append(items[0]);
		for (int i = 1; i < length; i++) {
			buffer.append(separator);
			buffer.append(items[i]);
		}
		return buffer.toString();
	}

	static public CharArray with(char... array) {
		return new CharArray(array);
	}

	public CharArray splice(int begin, int end) {
		CharArray longs = new CharArray(slice(begin, end));
		if (end - begin >= length) {
			items = new char[0];
			length = 0;
			return longs;
		} else {
			removeRange(begin, end - 1);
		}
		return longs;
	}

	public static char[] slice(char[] array, int begin, int end) {
		if (begin > end) {
			throw LSystem.runThrow("CharArray begin > end");
		}
		if (begin < 0) {
			begin = array.length + begin;
		}
		if (end < 0) {
			end = array.length + end;
		}
		int elements = end - begin;
		char[] ret = new char[elements];
		System.arraycopy(array, begin, ret, 0, elements);
		return ret;
	}

	public static char[] slice(char[] array, int begin) {
		return slice(array, begin, array.length);
	}

	public CharArray slice(int size) {
		return new CharArray(slice(this.items, size, this.length));
	}

	public CharArray slice(int begin, int end) {
		return new CharArray(slice(this.items, begin, end));
	}

	public static char[] concat(char[] array, char[] other) {
		return concat(array, array.length, other, other.length);
	}

	public static char[] concat(char[] array, int alen, char[] other, int blen) {
		char[] ret = new char[alen + blen];
		System.arraycopy(array, 0, ret, 0, alen);
		System.arraycopy(other, 0, ret, alen, blen);
		return ret;
	}

	public CharArray concat(CharArray o) {
		return new CharArray(concat(this.items, this.length, o.items, o.length));
	}

	@Override
	public int size() {
		return length;
	}

	@Override
	public boolean isEmpty() {
		return length == 0 || items == null;
	}

	public byte[] getBytes() {
		char[] items = this.items;
		byte[] bytes = new byte[this.length];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = BigInteger.valueOf(items[i]).byteValue();
		}
		return bytes;
	}

	public CharArray where(QueryEvent<Character> test) {
		CharArray list = new CharArray();
		for (int i = 0; i < length; i++) {
			Character t = Character.valueOf(get(i));
			if (test.hit(t)) {
				list.add(t);
			}
		}
		return list;
	}

	public Character find(QueryEvent<Character> test) {
		for (int i = 0; i < length; i++) {
			Character t = Character.valueOf(get(i));
			if (test.hit(t)) {
				return t;
			}
		}
		return null;
	}

	public boolean remove(QueryEvent<Character> test) {
		for (int i = length - 1; i > -1; i--) {
			Character t = get(i);
			if (test.hit(t)) {
				return removeValue(t);
			}
		}
		return false;
	}

	public String toString(char split) {
		if (length == 0) {
			return "[]";
		}
		char[] items = this.items;
		StringBuilder buffer = new StringBuilder(CollectionUtils.INITIAL_CAPACITY);
		buffer.append('[');
		buffer.append(items[0]);
		for (int i = 1; i < length; i++) {
			buffer.append(split);
			buffer.append(items[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	public String getString() {
		return new String(items);
	}

	@Override
	public String toString() {
		return toString(',');
	}
}