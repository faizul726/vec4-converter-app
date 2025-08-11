package dev.faizul726.vec4converter

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import dev.faizul726.vec4converter.ui.theme.Vec4ConverterTheme
import androidx.core.graphics.toColorInt
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Vec4ConverterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Home(
                        bday = "20250810_2106",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Home(bday: String, modifier: Modifier = Modifier) {
    val controller = rememberColorPickerController()
    var currentColor by remember { mutableStateOf("") }
    var useVec4 by remember { mutableStateOf(false) }
    var decimalDigits by remember { mutableIntStateOf(4) }
    var currentHex by remember { mutableStateOf("") }
    //controller.selectByColor(Color(0xFF0099FF), true)
    var showHexPopup by remember { mutableStateOf(false) }
    var showAboutPopup by remember { mutableStateOf(false) }
    var hexInput by remember { mutableStateOf("") }
    val hexInputFocus = remember { FocusRequester() }
    var includeConstructor by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    currentColor = toVec3(currentColor, decimalDigits, useVec4)

    var output by remember { mutableStateOf("") }

    output = if (includeConstructor) {
        "vec${if (useVec4) "4" else "3"}($currentColor)"
    } else {
        currentColor
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HsvColorPicker(
            controller = controller,
            modifier = Modifier
                .fillMaxWidth()
                .height(384.dp)
                .padding(horizontal = 16.dp),
            onColorChanged = {
                currentColor = it.color.toString()
                currentHex = it.hexCode.uppercase()
            }
        )
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(35.dp),
            controller = controller
        )
        Spacer(Modifier.height(10.dp))
        AnimatedVisibility(
            visible = useVec4
        ) {
            AlphaSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp)
                    .height(35.dp),
                controller = controller
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlphaTile(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                controller = controller
            )
            Column {
                Text(
                    text = if (useVec4) "Output:  vec3  / [vec4]" else "Output: [vec3] /  vec4 ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            useVec4 = !useVec4
                            currentColor = toVec3(controller.selectedColor.value.toString(), decimalDigits, useVec4)
                        }
                )
                Text(
                    text = if (decimalDigits == 4) "Decimal digits:  2  / [4]" else "Decimal digits: [2] /  4 ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            decimalDigits = if (decimalDigits == 4) 2 else 4
                            currentColor = toVec3(controller.selectedColor.value.toString(), decimalDigits, useVec4)
                        }
                )
                Text(
                    text = "Hex: #$currentHex",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            hexInput = currentHex
                            showHexPopup = true
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    hexInput = currentHex
                                    showHexPopup = true
                                },
                                onLongPress = {
                                    clipboardManager.setText(AnnotatedString(currentHex))
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                )
                Text(
                    text = "Include constructor: ${if (includeConstructor) "true " else "false"}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            includeConstructor = !includeConstructor
                        }
                )
            }
        }

        if (showHexPopup) {
            BasicAlertDialog(
                onDismissRequest = {
                    showHexPopup = false
                    hexInput = ""
                }
            ) {
                Surface(
                    modifier = Modifier
                        .width(280.dp), //https://m3.material.io/components/dialogs/specs
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                    //shadowElevation = 4.dp
                ) {
                    // Thanks https://stackoverflow.com/a/76889911/30810698
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Enter custom hex value", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = {
                                hexInput = it
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        hexInput = ""
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.backspace),
                                        contentDescription = null
                                    )
                                }
                            },
                            isError = !(hexInput.isNotBlank() && hexInput.matches(Regex("^#?([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})$"))),
                            label = { Text("RGB/RGBA") },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            singleLine = true,
                            maxLines = 1,
                            supportingText = { Text("Use #0099ff / ff0099eeff\nLong press Hex in main screen to copy")},
                            modifier = Modifier.focusRequester(hexInputFocus)
                        )
                        Spacer(Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            TextButton(
                                onClick = {
                                    showHexPopup = false
                                    hexInput = currentHex
                                }

                            ) {
                                Text("Cancel")
                            }
                            TextButton(
                                enabled = hexInput.isNotBlank() && hexInput.matches(Regex("^#?([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})$")),
                                onClick = {
                                    controller.selectByColor(Color("#$hexInput".replace("##", "#").toColorInt()), true)
                                    showHexPopup = false
                                },
                            ) {
                                Text("Done")
                            }
                        }
                    }
                }
            }
        }
        if (showAboutPopup) {
            BasicAlertDialog(
                onDismissRequest = { showAboutPopup = false }
            ) {
                Surface(
                    modifier = Modifier
                        .width(280.dp), //https://m3.material.io/components/dialogs/specs
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                    //shadowElevation = 4.dp
                ) {
                    // Thanks https://stackoverflow.com/a/76889911/30810698
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("About", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))
                        Text("Made by faizul726\nBorn on $bday", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
                        Row(
                            modifier = Modifier.clickable { uriHandler.openUri("https://github.com/faizul726/vec4-converter-app") },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Source code on GitHub",
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.padding(end = 4.dp),
                                softWrap = false,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                painterResource(R.drawable.open_in_new),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clickable { uriHandler.openUri("https://github.com/skydoves/colorpicker-compose") },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Color picker library by skydoves",
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.padding(end = 4.dp),
                                softWrap = false,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                painterResource(R.drawable.open_in_new),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            "Google Material Icons and colorpicker-compose is licensed under:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Row(
                            modifier = Modifier.clickable { uriHandler.openUri("https://www.apache.org/licenses/LICENSE-2.0.txt") },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Apache License Version 2.0",
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.padding(end = 4.dp),
                                softWrap = false,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                painterResource(R.drawable.open_in_new),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        TextButton(
                            onClick = { showAboutPopup = false },
                            modifier = Modifier.align(Alignment.End)

                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
        if (showHexPopup) {
            LaunchedEffect(Unit) {
                hexInputFocus.requestFocus()
                keyboardController?.show()
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,

        ) {
            Text(
                text = output,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 4.dp)
            )
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(output))
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.content_copy),
                    contentDescription = null,
                )
            }
        }
        /*Row(
            Modifier.clickable {
                clipboardManager.setText(AnnotatedString(output))
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.content_copy),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(12.dp)
            )
            Text("Copy to clipboard", fontSize = 12.sp)
        }*/
    }
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                showAboutPopup = true
            },
            modifier = modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                painter = painterResource(R.drawable.info),
                contentDescription = null
            )
        }
    }
}

private fun toVec3(hex: String, precision: Int, useVec4: Boolean): String {
    return Regex("-?\\d+(\\.\\d+)?").findAll(hex)
        .take(if (useVec4) 4 else 3)
        .map { it.value.toDouble() }
        .map { formatNumber(it, precision) }
        .toList()
        .joinToString()
}

fun formatNumber(num: Double, precision: Int): String {
    // Format to max 4 decimal places
    var str = "%.${precision}f".format(num).trimEnd('0').trimEnd('.')
    // Ensure at least 1 decimal place
    if (!str.contains('.')) str += ".0"
    return str
}
