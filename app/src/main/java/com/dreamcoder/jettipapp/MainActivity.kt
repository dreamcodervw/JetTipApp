package com.dreamcoder.jettipapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dreamcoder.jettipapp.components.InputField
import com.dreamcoder.jettipapp.ui.theme.JetTipAppTheme
import com.dreamcoder.jettipapp.widgets.RoundIconButton

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MainContent()
            }
        }
    }
}

// Container function
@Composable
fun MyApp(content: @Composable () -> Unit) {
    JetTipAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Composable
fun TopHeader(totalPerPerson: Double = 0.0) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 15.dp)
            .height(150.dp)
            .clip(shape = RoundedCornerShape(corner = CornerSize(12.dp))),
        color = Color(0xFFE9D7F7)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Total Per Person",
                style = MaterialTheme.typography.titleLarge
            )
            val total = "%.2f".format(totalPerPerson)
            Text(
                text = "$$total",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun MainContent() {
    Column {
        val totalPerPersonBill = remember { mutableDoubleStateOf(0.0) }
        TopHeader(totalPerPersonBill.doubleValue)
        BillForm(totalPerPersonBill)
    }
}

@Composable
fun BillForm(totalPerPersonBill: MutableDoubleState) {
    val totalBillState = remember { mutableStateOf("") }
    val keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
    Surface(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        border = BorderStroke(width = 1.dp, color = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(all = 6.dp)) {
            BillInputField(
                totalBillState,
                totalBillState.value.isNotEmpty(),
                keyboardController
            )
            if (totalBillState.value.isNotEmpty()) {
                TipCalculation(totalBillState.value) {
                    totalPerPersonBill.doubleValue = it
                }
            }
        }
    }
}

@Composable
private fun TipCalculation(totalBill: String, finalBillPerPerson: (Double) -> Unit) {
    val splitValue = remember { mutableIntStateOf(1) }
    val sliderPosition = remember { mutableFloatStateOf(0f) }
    val tipAmount = remember { mutableDoubleStateOf(0.0) }
    Split(splitValue)
    Tip(tipAmount)
    TextSlider(sliderPosition, tipAmount, totalBill)
    finalBillPerPerson((totalBill.toDouble() + tipAmount.doubleValue) / splitValue.intValue)
}

@Composable
fun TextSlider(
    sliderPosition: MutableFloatState,
    tipAmount: MutableDoubleState,
    totalBill: String
) {
    var tipPercentage = (sliderPosition.floatValue * 100).toInt()
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$tipPercentage%")
        Spacer(modifier = Modifier.height(14.dp))
        Slider(value = sliderPosition.floatValue, onValueChange = { newValue ->
            sliderPosition.floatValue = newValue
            tipPercentage = (sliderPosition.floatValue * 100).toInt()
            tipAmount.doubleValue =
                calculateTotalTip(totalBill.toDouble(), tipPercentage)
        }, modifier = Modifier.padding(start = 16.dp, end = 16.dp), steps = 5)
    }
}

fun calculateTotalTip(totalBill: Double, tipPercentage: Int): Double {
    return if (totalBill > 1) (totalBill * tipPercentage) / 100 else 0.0
}

@Composable
private fun Tip(tipAmount: MutableDoubleState) {
    Row(modifier = Modifier.padding(horizontal = 3.dp, vertical = 12.dp)) {
        Text(text = "Tip", modifier = Modifier.align(alignment = Alignment.CenterVertically))
        Spacer(modifier = Modifier.width(200.dp))
        Text(
            text = "$${tipAmount.doubleValue}",
            modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
    }
}

@Composable
private fun Split(splitValue: MutableIntState) {
    Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.Start) {
        Text(
            text = "Split",
            modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(120.dp))
        Row(
            modifier = Modifier.padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.End
        ) {
            RoundIconButton(
                imageVector = Icons.Default.Remove,
                onClick = {
                    if (splitValue.intValue > 1) {
                        splitValue.intValue -= 1
                    }
                })
            Text(
                text = "${splitValue.intValue}",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 9.dp, end = 9.dp)
            )
            RoundIconButton(
                imageVector = Icons.Default.Add,
                onClick = { splitValue.intValue += 1 })
        }
    }
}

@Composable
private fun BillInputField(
    totalBillState: MutableState<String>,
    validState: Boolean,
    keyboardController: SoftwareKeyboardController?
) {
    InputField(
        valueState = totalBillState,
        labelId = "Enter Bill",
        enabled = true,
        isSingleLine = true,
        onAction = KeyboardActions {
            if (!validState) {
                return@KeyboardActions
            } else {
                keyboardController?.hide()
            }
        }
    )
}