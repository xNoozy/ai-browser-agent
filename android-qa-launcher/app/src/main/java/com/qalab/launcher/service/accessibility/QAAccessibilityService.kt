package com.qalab.launcher.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QAAccessibilityService : AccessibilityService() {

    private val _uiTree = MutableStateFlow<AccessibilityNodeInfo?>(null)
    val uiTree: StateFlow<AccessibilityNodeInfo?> = _uiTree

    private val _events = MutableStateFlow<List<AccessibilityEvent>>(emptyList())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        _uiTree.value = rootInActiveWindow

        val currentEvents = _events.value.toMutableList()
        currentEvents.add(event)
        if (currentEvents.size > MAX_EVENT_BUFFER) {
            currentEvents.removeAt(0)
        }
        _events.value = currentEvents
    }

    override fun onInterrupt() {
        _uiTree.value = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    // UI Automation Methods (user-consented only)

    fun clickElement(nodeInfo: AccessibilityNodeInfo): Boolean {
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun longClickElement(nodeInfo: AccessibilityNodeInfo): Boolean {
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

    fun setText(nodeInfo: AccessibilityNodeInfo, text: String): Boolean {
        val arguments = android.os.Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun scrollForward(nodeInfo: AccessibilityNodeInfo): Boolean {
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    fun scrollBackward(nodeInfo: AccessibilityNodeInfo): Boolean {
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }

    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()

        dispatchGesture(gesture, null, null)
    }

    fun performTap(x: Float, y: Float) {
        val path = Path().apply {
            moveTo(x, y)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        dispatchGesture(gesture, null, null)
    }

    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes.firstOrNull()
    }

    fun findNodeById(viewId: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        return nodes.firstOrNull()
    }

    fun getUiHierarchy(): List<UiNode> {
        val root = rootInActiveWindow ?: return emptyList()
        return buildUiTree(root)
    }

    private fun buildUiTree(node: AccessibilityNodeInfo, depth: Int = 0): List<UiNode> {
        val result = mutableListOf<UiNode>()
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        result.add(
            UiNode(
                className = node.className?.toString() ?: "",
                text = node.text?.toString(),
                contentDescription = node.contentDescription?.toString(),
                viewId = node.viewIdResourceName,
                bounds = bounds,
                isClickable = node.isClickable,
                isScrollable = node.isScrollable,
                isEnabled = node.isEnabled,
                depth = depth
            )
        )

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            result.addAll(buildUiTree(child, depth + 1))
        }

        return result
    }

    companion object {
        var instance: QAAccessibilityService? = null
            private set

        private const val MAX_EVENT_BUFFER = 100
    }
}

data class UiNode(
    val className: String,
    val text: String?,
    val contentDescription: String?,
    val viewId: String?,
    val bounds: Rect,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val isEnabled: Boolean,
    val depth: Int
)
