package util

import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import com.daviddam.clickconnect.R

object ImageExtension {
    fun ImageView.loadImageOrDefault(
        url: String?,
        isProfile: Boolean = true
    ) {
        if (url.isNullOrBlank()) {
            if (isProfile) {
                load(R.drawable.avatar) {
                    transformations(CircleCropTransformation())
                }
            } else {
                setImageDrawable(null)
            }
            return
        }

        load(url) {
            crossfade(true)
            placeholder(null)
            if (isProfile) {
                transformations(CircleCropTransformation())
                error(R.drawable.avatar)
            }
        }
    }
}
