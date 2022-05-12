package de.jadehs.vcg.layout.fragments.map

import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import de.jadehs.vcg.data.model.Coordinate
import org.oscim.android.canvas.AndroidBitmap
import org.oscim.backend.canvas.Bitmap
import org.oscim.core.GeoPoint
import org.oscim.layers.marker.MarkerInterface
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.renderer.atlas.TextureAtlas
import org.oscim.renderer.atlas.TextureRegion
import org.oscim.renderer.bucket.TextureItem
import org.slf4j.Marker


private const val TAG = "WPMarker"


val GRAY_COLOR_MATRIX = floatArrayOf(
        0.25F, 0.25F, 0.25F, 0F, 50F,
        0.25F, 0.25F, 0.25F, 0F, 50F,
        0.25F, 0.25F, 0.25F, 0F, 50F,
        0F,0F,0F,0.6F,0F)
open class WPMarker:MarkerInterface {


    /**
     * the icon, which will get displayed on the map in the given size
     */
    var icon: Drawable? = null

    /**
     * location of the icon in earth coordinates
     */
    var position: GeoPoint = GeoPoint(0.0,0.0)
        set(value) {if(value.latitude > 0 && value.longitude > 0)field = value else throw IllegalArgumentException("negative coordinates are not allowed")}

    /**
     * the offset in percent (0-1) where the anchor point of the image should be from the left side
     *
     * if the value is zero the anchor will be the pixel at the very left. Default is in the middle of the icon
     */
    var anchorOffsetX = 0.5
        set(value) = if (value in 0.0..1.0) field = value else throw IllegalArgumentException("value is not between 1 and 0")

    /**
     * the offset in percent (0-1) where the anchor point of the image should be from the bottom side
     *
     * if the value is zero the anchor will be the pixel at the very top. Default is in the bottom of the icon
     */
    var anchorOffsetY = 1.0
        set(value) = if (value in 0.0..1.0) field = value else throw IllegalArgumentException("value is not between 1 and 0")


    /**
     * @deprecated
     * whether the icon is displayed gray and slightly transparent
     */
    var grayScale = false

    /**
     * margin of the clickable area to make click small icons easier (margin is applied on the top, bottom, left and right)
     */
    var margin = 0

//    /**
//     * the alpha value of the icon, if grayscale is on even 255 will be slightly transparent
//     */
//    var alpha = 255
//        set(value) {if(value in 0..255) field = value else throw IllegalArgumentException("Value is not between 0 and 255")}


    /**
     * the bounds of the clickable area
     */
    private val currentRect: RectF = RectF()


    fun setAnchorOffset(offsetX: Double, offsetY: Double){
        anchorOffsetX = offsetX
        anchorOffsetY = offsetY
    }
/*
    override fun onSingleTapUp(e: MotionEvent?, mapView: MapView?): Boolean {
        val cords = MotionEvent.PointerCoords()
        e?.getPointerCoords(0,cords)
        if(currentRect.contains(cords.x,cords.y)){
            return tapOnMarker(e,mapView)
        }
        return super.onSingleTapUp(e, mapView)
    }*/

    override fun getMarker(): MarkerSymbol? {
        icon?.let {
            val textItem = TextureItem(AndroidBitmap(it.toBitmap()))
            val rect = TextureAtlas.Rect(0,0,
                    textItem.width + margin*2,
                    textItem.height + margin*2)
            val textRegion = TextureRegion(textItem,rect)
            return MarkerSymbol(textRegion,anchorOffsetX.toFloat(),anchorOffsetY.toFloat())
        }
        return null
    }

    override fun getPoint(): GeoPoint {
        return position
    }
}