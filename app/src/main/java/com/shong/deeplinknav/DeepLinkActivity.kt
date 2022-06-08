package com.shong.deeplinknav

import android.app.ActivityManager
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.shong.deeplinknav.ui.theme.DeepLinkNavTheme

/**
 * 테스트는 ~% adb shell am start -a android.intent.action.VIEW -d "shong://first" "com.shong.deeplinknav"
 * scheme : shong
 * host : main, first, second
 */
class DeepLinkActivity : ComponentActivity() {
    val TAG = this::class.java.simpleName + "_sHong"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "isTaskRoot : $isTaskRoot  data : ${intent.data}")
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (at in manager.appTasks) {
            Log.d(TAG, "task : ${at.taskInfo}")
            val baseAct = at.taskInfo.baseActivity?.shortClassName
            Log.d(TAG, "baseAct : ${baseAct}")
        }

        handleDeepLink()
    }

    private fun handleDeepLink() {
        if (intent.action?.equals(Intent.ACTION_VIEW) == true) {
            Log.d(TAG, "DeepLink Intent Data : ${intent.data}")
            Log.d(TAG, "DeepLink Intent Scheme : ${intent.data?.scheme}")
            Log.d(TAG, "DeepLink Intent Host : ${intent.data?.host}")

            val data_str = intent.data.toString()
            when (intent.data?.host) {
                "main" -> {
                    val deepLinkIntent =
                        Intent(this@DeepLinkActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    startActivity(deepLinkIntent)
                    finish()
                }
                "first" -> {
                    val deepLinkIntent =
                        Intent(this@DeepLinkActivity, FirstActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    if (isTaskRoot) {
                        TaskStackBuilder.create(this@DeepLinkActivity).apply {
                            if (needAddMainForParent(deepLinkIntent)) {
                                val mainIntent =
                                    Intent(this@DeepLinkActivity, MainActivity::class.java).addFlags(
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    )
                                addNextIntentWithParentStack(mainIntent)
                            }

                            addNextIntent(deepLinkIntent)

                        }.startActivities()
                    } else {
                        startActivity(deepLinkIntent)
                    }
                    finish()
                }
                "second" -> {
                    val deepLinkIntent =
                        Intent(this@DeepLinkActivity, SecondActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    if (isTaskRoot) {
                        TaskStackBuilder.create(this@DeepLinkActivity).apply {
                            if (needAddMainForParent(deepLinkIntent)) {
                                val mainIntent =
                                    Intent(this@DeepLinkActivity, MainActivity::class.java).addFlags(
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    )
                                addNextIntentWithParentStack(mainIntent)
                            }

                            val coIntent =
                                Intent(this@DeepLinkActivity, FirstActivity::class.java).addFlags(
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                                )
                            addNextIntent(coIntent)

                            addNextIntent(deepLinkIntent)

                        }.startActivities()
                    } else {
                        startActivity(deepLinkIntent)
                    }
                    finish()
                }
                else -> {
                    Log.d(TAG, "Not Found Host : ${intent.data?.host}")
                    finishAffinity()
                }
            }
        } else {
            Log.d(TAG, "Wrong Action : ${intent.action}")
            finishAffinity()
        }
    }

    private fun needAddMainForParent(intent: Intent): Boolean =
        when (intent.component?.className) {
            MainActivity::class.java.name -> false
            else -> true
        }
}