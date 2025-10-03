import StatusBarUtils.hideSystemBars
import StatusBarUtils.isSystemBarsHidden
import StatusBarUtils.showSystemBars
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageScope
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.Transformation
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.placeholder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable


@Composable
fun Picture(
    modifier: Modifier = Modifier,
    model: Any?,
    transformations: List<Transformation> = emptyList(),
    manualImageRequest: ImageRequest? = null,
    manualImageLoader: ImageLoader? = null,
    contentDescription: String? = null,
    shape: Shape = CircleShape,
    contentScale: ContentScale = ContentScale.Crop,
    loading: @Composable (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Loading) -> Unit)? = null,
    success: @Composable (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Success) -> Unit)? = null,
    error: @Composable (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Error) -> Unit)? = null,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    zoomParams: ZoomParams = ZoomParams(),
    shimmerEnabled: Boolean = true,
    crossfadeEnabled: Boolean = true,
    allowHardware: Boolean = true,
) {
    val activity = LocalContext.current.findActivity()
    val context = LocalContext.current

    var errorOccurred by rememberSaveable { mutableStateOf(false) }

    var shimmerVisible by rememberSaveable { mutableStateOf(true) }

    val imageLoader =
        manualImageLoader ?: context.imageLoader.newBuilder().components {
            if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
            add(SvgDecoder.Factory())
        }.build()

    val request = manualImageRequest ?: ImageRequest.Builder(context)
        .data(model)
        .crossfade(crossfadeEnabled)
        .allowHardware(allowHardware)
        .transformations(transformations)
        .build()

    val zoomState = if (zoomParams.zoomEnabled) {
        rememberZoomState()
    } else null

    val image: @Composable () -> Unit = {
        SubcomposeAsyncImage(
            model = request,
            imageLoader = imageLoader,
            contentDescription = contentDescription,
            modifier = modifier
                .clip(shape)
                .then(if (shimmerEnabled) Modifier.shimmer(shimmerVisible) else Modifier)
                .then(if (zoomState != null) Modifier.zoomable(zoomState) else Modifier),
            contentScale = contentScale,
            loading = {
                if (loading != null) loading(it)
                shimmerVisible = true
            },
            success = success,
            error = error,
            onSuccess = {
                shimmerVisible = false
                onSuccess?.invoke(it)
                onState?.invoke(it)
            },
            onLoading = {
                onLoading?.invoke(it)
                onState?.invoke(it)
            },
            onError = {
                if (error != null) shimmerVisible = false
                onError?.invoke(it)
                onState?.invoke(it)
                errorOccurred = true
            },
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )
    }
    image()

    //Needed for triggering recomposition
    LaunchedEffect(errorOccurred) {
        if (errorOccurred && error == null) {
            shimmerVisible = false
            shimmerVisible = true
            errorOccurred = false
        }
    }

}

object StatusBarUtils {

    val Activity.isSystemBarsHidden: Boolean
        get() {
            return _isSystemBarsHidden
        }

    private var _isSystemBarsHidden = false

    fun Activity.hideSystemBars() = WindowInsetsControllerCompat(
        window,
        window.decorView
    ).let { controller ->
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        _isSystemBarsHidden = true
    }

    fun Activity.showSystemBars() = WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.systemBars()).also {
        _isSystemBarsHidden = false
    }
}

data class ZoomParams(
    val zoomEnabled: Boolean = false,
    val hideBarsOnTap: Boolean = false,
    val minZoomScale: Float = 1f,
    val maxZoomScale: Float = 4f,
    val onTap: (Offset) -> Unit = {}
)

private fun Dp.toPx(density: Density): Float {
    return with(density) { toPx() }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun Modifier.shimmer(
    visible: Boolean,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) = then(
    Modifier.placeholder(
        visible = visible,
        color = color,
        highlight = PlaceholderHighlight.shimmer()
    )
)
