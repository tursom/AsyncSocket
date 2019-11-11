package cn.tursom.utils

import java.util.concurrent.atomic.AtomicReference

class NonLockLinkedList<T> {
  private val root = AtomicReference<TaskListNode<T>?>()

  fun add(data: T) {
    val taskNode = TaskListNode(data, root.get())
    while (!root.compareAndSet(taskNode.next, taskNode)) {
      taskNode.next = root.get()
    }
  }

  operator fun invoke(data: T) = add(data)

  fun take(): T? {
    var node = root.get()
    while (!root.compareAndSet(node, node?.next)) {
      node = root.get()
    }
    return node?.data
  }

  fun isNotEmpty(): Boolean = root.get() != null

  private class TaskListNode<T>(
    val data: T,
    @Volatile var next: TaskListNode<T>?
  )
}