package com.retrivedmods.wclient.game.module.visual

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.Player
import com.retrivedmods.wclient.render.RenderOverlayView
import org.cloudburstmc.math.matrix.Matrix4f
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.math.vector.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class ESPModule : Module("esp", ModuleCategory.Visual) {
    companion object {
        private var renderView: RenderOverlayView? = null

        fun setRenderView(view: RenderOverlayView) {
            renderView = view
        }
    }

    // Basic settings
    private val fov by floatValue("fov", 110f, 40f..110f)
    private val strokeWidth by floatValue("stroke_width", 2f, 1f..10f)
    private val colorRed by intValue("color_red", 255, 0..255)
    private val colorGreen by intValue("color_green", 0, 0..255)
    private val colorBlue by intValue("color_blue", 0, 0..255)

    // Display options
    private val showAllEntities by boolValue("show_all_entities", false)
    private val showDistance by boolValue("show_distance", true)
    private val showNames by boolValue("show_names", true)

    // Box style options
    private val use2DBox by boolValue("2d_box", false)
    private val use3DBox by boolValue("3d_box", true)
    private val useCornerBox by boolValue("corner_box", false)

    // Tracer options
    private val tracers by boolValue("tracers", false)
    private val tracerBottom by boolValue("tracer_bottom", true)
    private val tracerTop by boolValue("tracer_top", false)
    private val tracerCenter by boolValue("tracer_center", false)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        // ESP doesn't need to intercept packets
    }

    override fun onEnabled() {
        super.onEnabled()
        renderView?.invalidate()
    }

    override fun onDisabled() {
        super.onDisabled()
        renderView?.invalidate()
    }

    private fun rotateX(angle: Float): Matrix4f {
        val rad = Math.toRadians(angle.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()

        return Matrix4f.from(
            1f, 0f, 0f, 0f,
            0f, c, -s, 0f,
            0f, s, c, 0f,
            0f, 0f, 0f, 1f
        )
    }

    private fun rotateY(angle: Float): Matrix4f {
        val rad = Math.toRadians(angle.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()

        return Matrix4f.from(
            c, 0f, s, 0f,
            0f, 1f, 0f, 0f,
            -s, 0f, c, 0f,
            0f, 0f, 0f, 1f
        )
    }

    private fun getEntityBoxVertices(entity: Entity): Array<Vector3f> {
        val width = 0.6f
        val height = 1.8f // Standard player/entity height

        val pos = entity.vec3Position
        val halfWidth = width / 2f

        val yPos = if (entity is Player) {
            pos.y - 1.62f
        } else {
            pos.y
        }

        return arrayOf(
            Vector3f.from(pos.x - halfWidth, yPos, pos.z - halfWidth),          // Bottom front left
            Vector3f.from(pos.x - halfWidth, yPos + height, pos.z - halfWidth), // Top front left
            Vector3f.from(pos.x + halfWidth, yPos + height, pos.z - halfWidth), // Top front right
            Vector3f.from(pos.x + halfWidth, yPos, pos.z - halfWidth),          // Bottom front right
            Vector3f.from(pos.x - halfWidth, yPos, pos.z + halfWidth),          // Bottom back left
            Vector3f.from(pos.x - halfWidth, yPos + height, pos.z + halfWidth), // Top back left
            Vector3f.from(pos.x + halfWidth, yPos + height, pos.z + halfWidth), // Top back right
            Vector3f.from(pos.x + halfWidth, yPos, pos.z + halfWidth)           // Bottom back right
        )
    }

    private fun worldToScreen(pos: Vector3f, viewProjMatrix: Matrix4f, screenWidth: Int, screenHeight: Int): Vector2f? {
        val w = viewProjMatrix.get(3, 0) * pos.x +
                viewProjMatrix.get(3, 1) * pos.y +
                viewProjMatrix.get(3, 2) * pos.z +
                viewProjMatrix.get(3, 3)

        if (w < 0.01f) return null

        val inverseW = 1f / w

        val screenX = screenWidth / 2f + (0.5f * ((viewProjMatrix.get(0, 0) * pos.x +
                viewProjMatrix.get(0, 1) * pos.y +
                viewProjMatrix.get(0, 2) * pos.z +
                viewProjMatrix.get(0, 3)) * inverseW) * screenWidth + 0.5f)

        val screenY = screenHeight / 2f - (0.5f * ((viewProjMatrix.get(1, 0) * pos.x +
                viewProjMatrix.get(1, 1) * pos.y +
                viewProjMatrix.get(1, 2) * pos.z +
                viewProjMatrix.get(1, 3)) * inverseW) * screenHeight + 0.5f)

        return Vector2f.from(screenX, screenY)
    }

    private fun shouldRenderEntity(entity: Entity): Boolean {
        if (entity == session.localPlayer) return false
        if (!showAllEntities && entity !is Player) return false
        return true
    }

    fun render(canvas: Canvas) {
        if (!isEnabled || !isSessionCreated) return  // isSessionCreated check

        val player = session.localPlayer
        val entities = if (showAllEntities) {
            session.level.entityMap.values
        } else {
            session.level.entityMap.values.filterIsInstance<Player>()
        }

        if (entities.isEmpty()) return

        val screenWidth = canvas.width
        val screenHeight = canvas.height

        val viewProjMatrix = Matrix4f.createPerspective(fov,
            screenWidth.toFloat() / screenHeight, 0.1f, 128f)
            .mul(Matrix4f.createTranslation(player.vec3Position)
                .mul(rotateY(-player.rotationYaw - 180))
                .mul(rotateX(-player.rotationPitch))
                .invert())

        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = this@ESPModule.strokeWidth
            color = Color.rgb(colorRed, colorGreen, colorBlue)
        }

        entities.forEach { entity ->
            if (shouldRenderEntity(entity)) {
                drawEntityBox(entity, viewProjMatrix, screenWidth, screenHeight, canvas, paint)
            }
        }
    }

    private fun drawEntityBox(entity: Entity, viewProjMatrix: Matrix4f,
                              screenWidth: Int, screenHeight: Int,
                              canvas: Canvas, paint: Paint) {
        val boxVertices = getEntityBoxVertices(entity)
        var minX = screenWidth.toDouble()
        var minY = screenHeight.toDouble()
        var maxX = 0.0
        var maxY = 0.0
        val screenPositions = mutableListOf<Vector2f>()

        boxVertices.forEach { vertex ->
            val screenPos = worldToScreen(vertex, viewProjMatrix, screenWidth, screenHeight)
                ?: return@forEach
            screenPositions.add(screenPos)
            minX = minX.coerceAtMost(screenPos.x.toDouble())
            minY = minY.coerceAtMost(screenPos.y.toDouble())
            maxX = maxX.coerceAtLeast(screenPos.x.toDouble())
            maxY = maxY.coerceAtLeast(screenPos.y.toDouble())
        }

        if (!(minX >= screenWidth || minY >= screenHeight || maxX <= 0 || maxY <= 0)) {
            when {
                use2DBox -> draw2DBox(canvas, paint, minX, minY, maxX, maxY)
                use3DBox -> draw3DBox(canvas, paint, screenPositions)
                useCornerBox -> drawCornerBox(canvas, paint, minX, minY, maxX, maxY)
            }

            if (tracers) {
                drawTracer(canvas, paint, screenWidth, screenHeight, minX, minY, maxX, maxY)
            }

            if (showNames || showDistance) {
                drawEntityInfo(canvas, paint, entity, minX, minY, maxX)
            }
        }
    }

    private fun draw2DBox(canvas: Canvas, paint: Paint, minX: Double, minY: Double, maxX: Double, maxY: Double) {
        val padding = paint.strokeWidth / 2
        canvas.drawRect(
            minX.toFloat() + padding,
            minY.toFloat() + padding,
            maxX.toFloat() - padding,
            maxY.toFloat() - padding,
            paint
        )
    }

    private fun draw3DBox(canvas: Canvas, paint: Paint, screenPositions: List<Vector2f>) {
        if (screenPositions.size < 8) return

        val edges = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0,  // Front face
            4 to 5, 5 to 6, 6 to 7, 7 to 4,  // Back face
            0 to 4, 1 to 5, 2 to 6, 3 to 7   // Connecting edges
        )

        edges.forEach { (start, end) ->
            val startPos = screenPositions[start]
            val endPos = screenPositions[end]

            if (isOnScreen(startPos, canvas) && isOnScreen(endPos, canvas)) {
                //slight padding
                val padding = paint.strokeWidth / 2
                canvas.drawLine(
                    startPos.x.coerceIn(padding, canvas.width - padding),
                    startPos.y.coerceIn(padding, canvas.height - padding),
                    endPos.x.coerceIn(padding, canvas.width - padding),
                    endPos.y.coerceIn(padding, canvas.height - padding),
                    paint
                )
            }
        }
    }

    private fun isOnScreen(pos: Vector2f, canvas: Canvas): Boolean {
        return pos.x >= 0 && pos.x <= canvas.width &&
                pos.y >= 0 && pos.y <= canvas.height
    }

    private fun drawCornerBox(canvas: Canvas, paint: Paint, minX: Double, minY: Double, maxX: Double, maxY: Double) {
        val cornerLength = (maxX - minX).toFloat() / 4
        val corners = listOf(
            minX to minY,
            maxX to minY,
            maxX to maxY,
            minX to maxY
        )

        corners.forEachIndexed { i, (x, y) ->
            val nextCorner = corners[(i + 1) % 4]

            // Horizontal lines
            canvas.drawLine(
                x.toFloat(),
                y.toFloat(),
                x.toFloat() + if (i % 2 == 0) cornerLength else -cornerLength,
                y.toFloat(),
                paint
            )

            // Vertical lines
            canvas.drawLine(
                x.toFloat(),
                y.toFloat(),
                x.toFloat(),
                y.toFloat() + if (i < 2) cornerLength else -cornerLength,
                paint
            )

            // Connect to next corner
            canvas.drawLine(
                x.toFloat() + if (i % 2 == 0) cornerLength else -cornerLength,
                y.toFloat(),
                nextCorner.first.toFloat(),
                nextCorner.second.toFloat(),
                paint
            )
        }
    }

    private fun drawTracer(canvas: Canvas, paint: Paint, screenWidth: Int, screenHeight: Int, minX: Double, minY: Double, maxX: Double, maxY: Double) {
        val start = when {
            tracerBottom -> Vector2f.from(screenWidth / 2f, screenHeight.toFloat())
            tracerTop -> Vector2f.from(screenWidth / 2f, 0f)
            tracerCenter -> Vector2f.from(screenWidth / 2f, screenHeight / 2f)
            else -> Vector2f.from(screenWidth / 2f, screenHeight.toFloat())
        }
        val end = Vector2f.from(
            (minX + maxX).toFloat() / 2,
            (minY + maxY).toFloat() / 2
        )
        canvas.drawLine(
            start.x,
            start.y,
            end.x,
            end.y,
            paint
        )
    }

    @SuppressLint("DefaultLocale")
    private fun drawEntityInfo(canvas: Canvas, paint: Paint, entity: Entity, minX: Double, minY: Double, maxX: Double) {
        // Background paint for text
        val bgPaint = Paint().apply {
            color = Color.argb(160, 0, 0, 0) // Semi-transparent black background
            style = Paint.Style.FILL
        }

        // Outline paint
        val outlinePaint = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 4f // Thick outline
        }

        val textPaint = Paint().apply {
            color = paint.color
            textSize = 30f
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
        }

        val info = buildString {
            if (showNames && entity is Player) {
                append(entity.username)
            }
            if (showDistance) {
                if (isNotEmpty()) append(" | ")
                val distance = entity.vec3Position.distance(session.localPlayer.vec3Position)
                append("${String.format("%.1f", distance)}m")
            }
        }

        val textX = (minX + maxX).toFloat() / 2
        val textY = minY.toFloat() - 10

        val bounds = android.graphics.Rect()
        textPaint.getTextBounds(info, 0, info.length, bounds)

        val padding = 8f
        val bgRect = android.graphics.RectF(
            textX - bounds.width() / 2 - padding,
            textY - bounds.height() - padding,
            textX + bounds.width() / 2 + padding,
            textY + padding
        )
        canvas.drawRoundRect(bgRect, 4f, 4f, bgPaint)

        canvas.drawText(info, textX, textY, outlinePaint)

        canvas.drawText(info, textX, textY, textPaint)
    }
}