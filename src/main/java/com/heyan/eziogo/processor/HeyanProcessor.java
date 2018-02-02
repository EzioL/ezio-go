package com.heyan.eziogo.processor;

import com.heyan.eziogo.annotations.HeyanBindClass;
import com.heyan.eziogo.annotations.HeyanBindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static javax.lang.model.element.ElementKind.PACKAGE;

/**
 * Here be dragons Created by Ezio on 2018/1/30 下午2:24
 */

public class HeyanProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(HeyanBindClass.class)) {
            generateBinderClass((TypeElement) element);
        }

        return true;
    }

    private void generateBinderClass(TypeElement element) {
        ClassName className = ClassName.get(element);
        ParameterSpec parameterSpec = ParameterSpec.builder(className, "bean")
            .addModifiers(Modifier.FINAL)
            .build();
        List<VariableElement> bindViewFieldList = getFieldElementsWithAnnotation(element, HeyanBindView.class);
        CodeBlock.Builder bindViewCodeBlockBuilder = CodeBlock.builder();
        for (VariableElement variableElement : bindViewFieldList) {
            //注解的值，也就是view要赋的值
            int value = variableElement.getAnnotation(HeyanBindView.class).value();
            bindViewCodeBlockBuilder.addStatement("bean.setId($L)", value);
        }
        //生成bindView()方法
        MethodSpec bindViewMethod = MethodSpec.methodBuilder("bindView")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(parameterSpec)
            .addCode(bindViewCodeBlockBuilder.build())
            .returns(void.class)
            .build();

        //构造函数，内部调用bindView方法
        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(parameterSpec)
            .addStatement("$N($L)", bindViewMethod, parameterSpec.name)
            .build();

        //生成BinderDelegate类
        String binderClassName = element.getSimpleName().toString();

        TypeSpec delegateType = TypeSpec.classBuilder(binderClassName + "DelegateBinder")
            .addModifiers(Modifier.PUBLIC)
            .addMethod(bindViewMethod)
            .addMethod(constructorMethod)
            .build();

        JavaFile javaFile = JavaFile.builder(getPackage(element).getQualifiedName().toString(), delegateType)
            .addFileComment("This file is generated by Binder, do not edit!")
            .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<VariableElement> getFieldElementsWithAnnotation(TypeElement typeElement, Class<HeyanBindView> clazz) {
        List<VariableElement> elements = new ArrayList<>();
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getAnnotation(clazz) != null) {
                //并没有进行类型、访问权限检查，真实生产环境肯定是要检查的
                elements.add((VariableElement) element);
            }
        }
        return elements;
    }

    /**
     * 查找包名
     */
    public static PackageElement getPackage(Element element) {
        while (element.getKind() != PACKAGE) {
            element = element.getEnclosingElement();
        }
        return (PackageElement) element;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(HeyanBindClass.class.getCanonicalName());
    }
}