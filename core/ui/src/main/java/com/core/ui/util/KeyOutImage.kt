import android.graphics.ImageDecoder
import android.graphics.Movie
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun InstantGif(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Synchronously decode the drawable once at composition
    val animatedDrawable = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // On API 28+, ImageDecoder returns an AnimatedImageDrawable for GIFs
            val src = ImageDecoder.createSource(context.resources, resId)
            ImageDecoder.decodeDrawable(src) as AnimatedImageDrawable
        } else {
            // Fallback for older Android: decode into a Movie
            @Suppress("DEPRECATION")
            val input = context.resources.openRawResource(resId)
            Movie.decodeStream(input).apply {
                input.close()
            }
        }
    }

    // Ensure it starts animating when we first see it
    LaunchedEffect(animatedDrawable) {
        when (animatedDrawable) {
            is AnimatedImageDrawable -> animatedDrawable.start()
            is Movie -> {
                // For Movie, we need a custom drawing loop…
                // (see https://developer.android.com/reference/android/graphics/Movie)
            }
        }
    }

    // Host it in an ImageView so it renders immediately
    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                setImageDrawable(animatedDrawable as Drawable?)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = {
            // In case you ever recompose with a new drawable…
            it.setImageDrawable(animatedDrawable as Drawable?)
        },
        modifier = modifier
    )
}
