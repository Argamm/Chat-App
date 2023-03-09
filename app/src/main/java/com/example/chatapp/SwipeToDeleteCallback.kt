package com.example.chatapp

import android.animation.*
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Math.abs

open class SwipeToDeleteCallback(
    val adapter: MessageAdapter,
    messageRecyclerView: RecyclerView
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT
) {
    private val recyclerView = messageRecyclerView

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val item = adapter.getItem(position)
        val ref = FirebaseDatabase.getInstance().getReference("messages")
        val query: Query = ref.orderByChild("id").equalTo(item.id.toDouble())

        // Remove the red color from the item view
        viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, android.R.color.transparent))
//        viewHolder.itemView.alpha = 1f

        // Check if the message was sent by the current user
        if (item.senderId != FirebaseAuth.getInstance().currentUser?.uid) {
            // If not, don't allow it to be deleted
            // adapter.notifyItemChanged(position, null)
            val adapter = recyclerView.adapter
            adapter?.notifyItemChanged(viewHolder.adapterPosition)
            viewHolder.itemView.setBackgroundColor(0) // Reset the background color
            return
        }

        // Show the delete confirmation dialog
        val builder = AlertDialog.Builder(viewHolder.itemView.context)
        builder.setMessage("Are you sure you want to delete this message?")
        builder.setPositiveButton("Delete") { dialog, _ ->
            // Delete the message from the database
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                    }
                    adapter.notifyItemRemoved(position) // Remove the swiped item and notify the adapter to update the view
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TAG_onCanceled", "onCancelled", databaseError.toException())
                }
            })
            dialog.dismiss()

            // Reset the background color after the dialog is dismissed
            viewHolder.itemView.setBackgroundColor(0)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Reset the item view's position and alpha
            val itemView = viewHolder.itemView
            val animator = itemView.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(AccelerateInterpolator())

            // Change the background color gradually to the original color
            val originalColor = itemView.resources.getColor(R.color.white)
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, originalColor)
            colorAnimation.addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                itemView.setBackgroundColor(color)
            }
            colorAnimation.duration = 500

            animator.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    itemView.setBackgroundColor(originalColor) // Set the background color to the original color
                }
            })
            // Notify the adapter to update the view
//            adapter.notifyItemChanged(viewHolder.adapterPosition)
            // Start the animations
            animator.start()
            colorAnimation.start()

            dialog.dismiss()
        }
        builder.show()

        val adapter = recyclerView.adapter
        adapter?.notifyItemChanged(viewHolder.adapterPosition)
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.height
        val itemWidth = itemView.width
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }
        val deleteIcon =
            ContextCompat.getDrawable(recyclerView.context, R.drawable.baseline_delete_forever_24)!!
        val intrinsicWidth = deleteIcon.intrinsicWidth
        val intrinsicHeight = deleteIcon.intrinsicHeight

        val background = ColorDrawable(Color.RED)
        val backgroundColor = Color.parseColor("#f44336")
        background.color = backgroundColor
        val iconMargin = (itemHeight - intrinsicHeight) / 2
        val iconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val iconBottom = iconTop + intrinsicHeight

        // Reset the translationX and alpha properties
        itemView.translationX = 0f
        itemView.alpha = 1f

        if (!viewHolder.itemView.isPressed) {
            // Draw the swipe action background
            if (dX < 0) { // Swiping to the left
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
            } else { // Swiping to the right
                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
            }
            background.draw(c)

            // Draw the delete icon
            deleteIcon.setBounds(
                if (dX < 0) itemView.right - iconMargin - intrinsicWidth else itemView.left + iconMargin,
                iconTop,
                if (dX < 0) itemView.right - iconMargin else itemView.left + iconMargin + intrinsicWidth,
                iconBottom
            )
            deleteIcon.draw(c)
        }

        // Translate the item view
        val translationX = dX * 0.5f
        itemView.translationX = translationX

        // Fade out the item view as it's swiped away
        val alpha = 1 - abs(dX) / itemWidth.toFloat()
        itemView.alpha = alpha

        // Call super.onChildDraw at the end
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
