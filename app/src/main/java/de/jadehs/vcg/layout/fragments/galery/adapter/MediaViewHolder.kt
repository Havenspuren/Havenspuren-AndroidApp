package de.jadehs.vcg.layout.fragments.galery.adapter

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class MediaViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {


    abstract fun bind(mediaUri:Uri)
}