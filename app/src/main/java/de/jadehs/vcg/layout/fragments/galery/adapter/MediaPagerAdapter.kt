package de.jadehs.vcg.layout.fragments.galery.adapter

import android.net.Uri
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

val pictureFileEndings = listOf("png", "jpg", "jpeg", "bmp", "gif", "webp", "heic", "heif")
val videoFileEndings = listOf("3gp", "mp4", "mkv", "mkv", "webm")

class MediaPagerAdapter(uriList: List<Uri>, val fragmentManager: FragmentManager) : RecyclerView.Adapter<MediaViewHolder>() {

    private val mediaList = uriList.toTypedArray()


    override fun getItemCount(): Int = mediaList.size


    override fun getItemViewType(position: Int): Int {
        val mediaUri = mediaList[position]
        val isPicture = mediaUri.lastPathSegment?.let { path ->
            pictureFileEndings.any {
                path.endsWith(it, true)
            }
        } ?: false // if path not null check endsWith, false otherwise is what ?: does
        val isVideo = mediaUri.lastPathSegment?.let { path ->
            videoFileEndings.any {
                path.endsWith(it, true)
            }
        } ?: false // if path not null check endsWith, false otherwise is what ?: does
        return if (isPicture) 0 else if (isVideo) 1 else -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        return when (viewType) {
            0 -> PictureViewHolder(parent, fragmentManager)
            1 -> VideoViewHolder(parent)
            else -> throw IllegalStateException("View Type isn't a valid id -> media uri doesn't have a valid file type")
        }
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaUri = mediaList[position]
        holder.bind(mediaUri)
    }
}