package cn.leo.annotationprocessor;

import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cn.leo.annotation_lib.BindView;
import cn.leo.annotation_lib.ButterKnife;

@Keep
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvTest)
    TextView tvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        tvTest.setText("测试注解处理器成功!");
    }
}
