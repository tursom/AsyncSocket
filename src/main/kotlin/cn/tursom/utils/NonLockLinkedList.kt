package cn.tursom.utils

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

open class NonLockLinkedList<T> : BlockingQueue<T> {
  override val size: Int get() = throw NotSupportedException()
  private val root = AtomicReference<TaskListNode<T>?>()

  override infix fun add(element: T): Boolean {
    put(element)
    return true
  }

  infix operator fun invoke(data: T) = add(data)

  override fun take(): T? {
    var node = root.get()
    while (!root.compareAndSet(node, node?.next)) {
      node = root.get()
    }
    return node?.data
  }

  override fun contains(element: T): Boolean {
    throw NotSupportedException()
  }

  override fun addAll(elements: Collection<T>): Boolean {
    throw NotSupportedException()
  }

  override fun clear() {
    throw NotSupportedException()
  }

  override fun element(): T = take() ?: throw NoSuchElementException()

  override fun removeAll(elements: Collection<T>): Boolean {
    throw NotSupportedException()
  }

  override fun offer(e: T): Boolean {
    put(e)
    return true
  }

  override fun offer(e: T, timeout: Long, unit: TimeUnit): Boolean {
    throw NotSupportedException()
  }

  override fun iterator(): MutableIterator<T> {
    throw NotSupportedException()
  }

  override fun peek(): T {
    return root.get()?.data ?: throw NoSuchElementException()
  }

  override fun put(e: T) {
    val taskNode = TaskListNode(e, root.get())
    while (!root.compareAndSet(taskNode.next, taskNode)) {
      taskNode.next = root.get()
    }
  }

  override fun isEmpty(): Boolean {
    return root.get() == null
  }

  override fun remove(element: T): Boolean = throw NotSupportedException()

  override fun remove(): T? = take()

  override fun containsAll(elements: Collection<T>): Boolean {
    throw NotSupportedException()
  }

  override fun drainTo(c: MutableCollection<in T>): Int {
    throw NotSupportedException()
  }

  override fun drainTo(c: MutableCollection<in T>, maxElements: Int): Int {
    throw NotSupportedException()
  }

  override fun retainAll(elements: Collection<T>): Boolean {
    throw NotSupportedException()
  }

  override fun remainingCapacity(): Int {
    throw NotSupportedException()
  }

  override fun poll(timeout: Long, unit: TimeUnit): T? {
    throw NotSupportedException()
  }

  override fun poll(): T? = take()

  private class TaskListNode<T>(
      val data: T,
      @Volatile var next: TaskListNode<T>?
  )

  class NotSupportedException : Exception()
}