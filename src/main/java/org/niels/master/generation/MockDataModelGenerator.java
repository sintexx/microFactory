package org.niels.master.generation;

import com.squareup.javapoet.*;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.lang.model.element.Modifier;
import javax.persistence.Entity;
import java.io.File;
import java.io.IOException;

public class MockDataModelGenerator {
    public static ClassName generateMockDataModel(File javaFolder) {
        var mockTypeBuilder = TypeSpec.classBuilder("MockEntity")
                .addModifiers(Modifier.PUBLIC).superclass(PanacheEntity.class).addAnnotation(Entity.class);

        var nameField = FieldSpec.builder(String.class, "name").addModifiers(Modifier.PRIVATE).build();

        mockTypeBuilder.addField(nameField);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "name")
                .addStatement("this.$N = $N", "name", "name")
                .build();

        mockTypeBuilder.addMethod(constructor);

        mockTypeBuilder.addAnnotation(Data.class).addAnnotation(NoArgsConstructor.class);

        var f = CodeGenUtils.writeToJavaFile(javaFolder, mockTypeBuilder.build(), "org.niels.master.generated.model");

        return ClassName.get(f.packageName,
                f.typeSpec.name);
    }
}
