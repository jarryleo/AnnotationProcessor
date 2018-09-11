package cn.leo.annotation_lib;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> annotationSet = new LinkedHashSet<>();
        annotationSet.add(BindView.class.getCanonicalName());
        return annotationSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            //获取包名
            PackageElement packageElement = mElementUtils.getPackageOf(element);
            String pkgName = packageElement.getQualifiedName().toString();
            note("package = %s", pkgName);
            //类名
            String simpleName = element.getSimpleName().toString();
            //获取包装类类型
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            //String enclosingName = enclosingElement.getQualifiedName().toString();
            String enclosingName = enclosingElement.getSimpleName().toString();
            note("enclosingClass = %s", enclosingElement);

            //因为BindView只作用于filed，所以这里可直接进行强转
            VariableElement bindViewElement = (VariableElement) element;
            //3.获取注解的成员变量名
            String bindViewFiledName = bindViewElement.getSimpleName().toString();
            //3.获取注解的成员变量类型
            //String bindViewFiledClassType = bindViewElement.asType().toString();
            String bindViewFiledClassType = bindViewElement.asType().toString();

            //4.获取注解元数据
            BindView bindView = element.getAnnotation(BindView.class);
            int id = bindView.value();
            note("%s %s = %d", bindViewFiledClassType, bindViewFiledName, id);

            //4.生成文件
            createFile(pkgName, enclosingName, bindViewFiledClassType, bindViewFiledName, id);
            return true;
        }
        return false;
    }

    private void createFile(String pkgName, String simpleName, String bindViewFiledClassType, String bindViewFiledName, int id) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(ClassName.get(pkgName, simpleName + "$Binding"))
                .addModifiers(Modifier.PUBLIC);
        //.addAnnotation("android.support.annotation.Keep");
        //添加构造方法
        ClassName className = ClassName.get(pkgName, simpleName);
        typeBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(className, "activity")
                .addStatement("$N($N)",
                        "bindView", "activity")
                .build());

        //添加bindView成员方法
        MethodSpec.Builder bindViewBuilder = MethodSpec.methodBuilder("bindView")
                .addModifiers(Modifier.PRIVATE)
                .returns(TypeName.VOID)
                .addParameter(className, "activity");

        //添加方法内容
        bindViewBuilder.addStatement("$N.$N=$N.findViewById($L)",
                "activity",
                bindViewFiledName,
                "activity",
                id
        );

        typeBuilder.addMethod(bindViewBuilder.build());
        TypeSpec build = typeBuilder.build();

        try {
            // build com.example.HelloWorld.java
            JavaFile javaFile = JavaFile.builder(pkgName, build)
                    .addFileComment(" This codes are generated automatically. Do not modify!")
                    .build();
            // write to file
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 打印日志
     *
     * @param msg 消息
     */
    private void note(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    /**
     * 格式化打印日志
     *
     * @param format 格式
     * @param args   参数
     */
    private void note(String format, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
    }
}
