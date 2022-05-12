package de.jadehs.vcg.layout.fragments.galery.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import de.jadehs.vcg.R

class PictureViewHolder(parent: ViewGroup) : MediaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.picture_view, parent, false)) {

    val imageView: ImageView = itemView.findViewById(R.id.imageView)


    override fun bind(mediaUri: Uri) {
        imageView.setImageURI(mediaUri)
    }


}