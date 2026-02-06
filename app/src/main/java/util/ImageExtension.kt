package util

import android.widget.ImageView
import coil.load
import com.daviddam.clickconnect.R

object ImageExtension {
    fun ImageView.loadImageOrDefault(
        url: String?,
        defaultDrawable: Int = R.drawable.avatar
    ) {
        if (url.isNullOrEmpty()) {
            setImageResource(defaultDrawable)
            return
        }

        try {
            load(url) {
                crossfade(true)
                error(defaultDrawable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setImageResource(defaultDrawable)
        }
    }
}
