package com.candyblast.game

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import kotlin.random.Random
import kotlin.math.abs
import kotlin.math.min

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(44, 20, 86)
        window.navigationBarColor = Color.rgb(44, 20, 86)
        setContentView(CandyGameView(this))
    }
}

class CandyGameView(context: android.content.Context) : View(context) {

    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random(System.currentTimeMillis())

    private val rows = 8
    private val cols = 8
    private val colors = intArrayOf(
        Color.rgb(255, 74, 112),
        Color.rgb(255, 190, 45),
        Color.rgb(64, 211, 133),
        Color.rgb(62, 174, 255),
        Color.rgb(178, 103, 255),
        Color.rgb(255, 112, 210)
    )

    private val board = Array(rows) { IntArray(cols) { random.nextInt(colors.size) } }

    private var score = 0
    private var moves = 25
    private var message = "Match 3 candies!"
    private var startX = 0f
    private var startY = 0f
    private var cell = 0f
    private var boardLeft = 0f
    private var boardTop = 0f

    init {
        repeat(10) { clearMatches(false) }
    }

    private fun dp(value: Float) = value * density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(44, 20, 86))

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = dp(30f)
        paint.typeface = android.graphics.Typeface.create(
            "sans-serif-black", android.graphics.Typeface.BOLD
        )
        canvas.drawText("CANDY BLAST", width / 2f, dp(48f), paint)

        paint.textSize = dp(15f)
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText("SCORE: $score", width * 0.27f, dp(88f), paint)
        canvas.drawText("MOVES: $moves", width * 0.73f, dp(88f), paint)

        paint.color = Color.rgb(255, 218, 105)
        paint.textSize = dp(14f)
        canvas.drawText(message, width / 2f, dp(123f), paint)

        cell = min(width / cols.toFloat(), (height - dp(190f)) / rows)
            .coerceAtLeast(dp(20f))
        boardLeft = (width - cell * cols) / 2f
        boardTop = dp(150f)

        paint.color = Color.rgb(71, 43, 112)
        canvas.drawRoundRect(
            RectF(
                boardLeft - dp(5f),
                boardTop - dp(5f),
                boardLeft + cell * cols + dp(5f),
                boardTop + cell * rows + dp(5f)
            ), dp(18f), dp(18f), paint
        )

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = boardLeft + c * cell
                val top = boardTop + r * cell
                val inset = cell * 0.09f

                paint.color = Color.rgb(91, 62, 131)
                canvas.drawRoundRect(
                    RectF(left + 1, top + 1, left + cell - 1, top + cell - 1),
                    cell * 0.2f, cell * 0.2f, paint
                )

                val color = colors[board[r][c]]
                paint.color = color
                canvas.drawCircle(
                    left + cell / 2f,
                    top + cell / 2f,
                    cell * 0.36f,
                    paint
                )

                paint.color = Color.argb(105, 255, 255, 255)
                canvas.drawOval(
                    RectF(
                        left + cell * 0.29f,
                        top + cell * 0.22f,
                        left + cell * 0.51f,
                        top + cell * 0.34f
                    ), paint
                )
            }
        }

        paint.color = Color.WHITE
        paint.textSize = dp(12f)
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText(
            "Swipe candies to match 3 or more",
            width / 2f,
            min(height - dp(18f), boardTop + cell * rows + dp(28f)),
            paint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (moves <= 0) {
                    message = "Game over! Tap to restart."
                    score = 0
                    moves = 25
                    repeat(10) { clearMatches(false) }
                    invalidate()
                    return true
                }

                val dx = event.x - startX
                val dy = event.y - startY

                val c = ((startX - boardLeft) / cell).toInt()
                val r = ((startY - boardTop) / cell).toInt()

                if (r in 0 until rows && c in 0 until cols &&
                    (abs(dx) > dp(12f) || abs(dy) > dp(12f))
                ) {
                    val nr = r + if (abs(dy) > abs(dx)) {
                        if (dy > 0) 1 else -1
                    } else 0

                    val nc = c + if (abs(dx) >= abs(dy)) {
                        if (dx > 0) 1 else -1
                    } else 0

                    if (nr in 0 until rows && nc in 0 until cols) {
                        val temp = board[r][c]
                        board[r][c] = board[nr][nc]
                        board[nr][nc] = temp

                        if (hasMatch()) {
                            moves--
                            clearMatches(true)
                            message = if (moves == 0) {
                                "Round over! Tap to play again."
                            } else {
                                "Great match!"
                            }
                        } else {
                            board[nr][nc] = board[r][c]
                            board[r][c] = temp
                            message = "Try another move!"
                        }
                    }
                }

                invalidate()
                return true
            }
        }
        return true
    }

    private fun hasMatch(): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val value = board[r][c]

                if (c + 2 < cols &&
                    board[r][c + 1] == value &&
                    board[r][c + 2] == value
                ) return true

                if (r + 2 < rows &&
                    board[r + 1][c] == value &&
                    board[r + 2][c] == value
                ) return true
            }
        }
        return false
    }

    private fun clearMatches(addScore: Boolean) {
        var changed: Boolean

        do {
            val matched = Array(rows) { BooleanArray(cols) }
            changed = false

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val v = board[r][c]
                    var end = c + 1

                    while (end < cols && board[r][end] == v) end++
                    if (end - c >= 3) {
                        for (x in c until end) matched[r][x] = true
                    }

                    end = r + 1
                    while (end < rows && board[end][c] == v) end++
                    if (end - r >= 3) {
                        for (y in r until end) matched[y][c] = true
                    }
                }
            }

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (matched[r][c]) {
                        board[r][c] = -1
                        changed = true
                        if (addScore) score += 10
                    }
                }
            }

            if (changed) {
                for (c in 0 until cols) {
                    var write = rows - 1

                    for (r in rows - 1 downTo 0) {
                        if (board[r][c] >= 0) {
                            board[write][c] = board[r][c]
                            write--
                        }
                    }

                    while (write >= 0) {
                        board[write][c] = random.nextInt(colors.size)
                        write--
                    }
                }
            }
        } while (changed)
    }
}
