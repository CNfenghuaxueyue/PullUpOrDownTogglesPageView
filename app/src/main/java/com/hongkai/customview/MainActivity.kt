package com.hongkai.customview

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.hongkai.customview.view.zhihu.Adapter
import com.hongkai.customview.view.zhihu.PullUpOrDownTogglesPageView
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    var pullUpOrDownTogglesPageView: PullUpOrDownTogglesPageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pullUpOrDownTogglesPageView =
            findViewById<PullUpOrDownTogglesPageView>(R.id.pud_togglesPageView)
        var dp2px = pullUpOrDownTogglesPageView?.dp2px(this, 100f)
        pullUpOrDownTogglesPageView?.setPullUpDownLisenter(object :
            PullUpOrDownTogglesPageView.PullUpDownLisenter {
            override fun onPullUp(
                pullUpOrDownTogglesPageView: PullUpOrDownTogglesPageView?,
                formPage: Int,
                toPage: Int
            ) {

            }

            override fun onPullDown(
                pullUpOrDownTogglesPageView: PullUpOrDownTogglesPageView?,
                formPage: Int,
                toPage: Int
            ) {

            }

        })
        var list: ArrayList<String> = ArrayList()
        list.add("zhang Ting")
        list.add("li shu qian")
        list.add("zhang xin yu")
        list.add("zhou si ping")
        pullUpOrDownTogglesPageView?.setAdapter(object : Adapter<String>(list) {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun getView(context: Context?, position: Int, parent: ViewGroup?): View {
                var textView = TextView(context)
                textView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
//                    dp2px!!.toInt()
                )
                textView.setBackgroundColor(getColor(R.color.teal_200))
                textView.gravity = Gravity.CENTER
                var item = getItem(position)
                textView.setText("position = " + item)
                return textView;
            }

        })

        list.add("fan bing bing")
        list.add("jia qin")
        list.add(0, "秦岚")
        pullUpOrDownTogglesPageView?.notifyDataChange()

    }
}