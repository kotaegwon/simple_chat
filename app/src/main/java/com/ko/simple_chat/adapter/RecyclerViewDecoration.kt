package com.ko.simple_chat.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ko.simple_chat.R

class RecyclerViewDecoration(
    context: Context,
    divHeight: Int,
    boundaryLineHeight: Int
) : RecyclerView.ItemDecoration() {

    private val divHeightPx: Int
    private val boundaryLineHeightPx: Int
    private val paint: Paint = Paint()

    init {
        paint.color = ContextCompat.getColor(context, R.color.orange)

        divHeightPx = dpToPx(context, divHeight)
        boundaryLineHeightPx = dpToPx(context, boundaryLineHeight)
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        // 아래쪽에 divider 공간 확보
        outRect.bottom = divHeightPx
    }

    override fun onDrawOver(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDrawOver(c, parent, state)

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until parent.childCount - 1) {

            val child = parent.getChildAt(i)

            val top = child.bottom
            val bottom = top + boundaryLineHeightPx

            c.drawRect(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                paint
            )
        }
    }
}
