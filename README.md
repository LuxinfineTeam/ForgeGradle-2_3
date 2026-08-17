# ForgeGradle 2.3 extended
Это форк проекта https://github.com/anatawa12/ForgeGradle-2.3/

### Список изменений:
- Поддержка запуска gradle на JDK 17-21
- Использование org.glavo:pack200:0.3.0 для замены удаленного Pack200 класса из новых версий Java
- Небольшие оптимизации общей работы плагина за счет перехода на java nio в некоторых местах
- Подготовка к поддержке современных версий Gradle (9.0+) через ExecHelper
- Fernflower декомпилятор теперь запускается всегда в fork-режиме через JDK8. К сожалению, с jdk17+ он не работает, а обновить версию fernflower нельзя, это ломает совместимость выходного декомпил кода с MCP патчами forge
- useDepAts опция теперь сканирует _at.cfg по всему jar, а не только то, что прописано в манифесте. Это решает проблему некоторых модов, которые регистрируют AT программно, по типу ThermalMods
- Удаление extractDependencyATs таска, используется своя реализация для сканирования AT в зависимостях
- Обновление OW2 ASM в класспазе плагина до версии 9.10.1

### Пример подключения плагина:
```groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
        maven {
            name 'forge'
            url 'https://maven.minecraftforge.net'
        }
    }
    dependencies {
        classpath('com.github.LuxinfineTeam:ForgeGradle-2_3:main-SNAPSHOT') {
            changing = true
        }
    }
}

apply plugin: 'net.minecraftforge.gradle.forge'

[compileJava, compileTestJava]*.options*.encoding = 'UTF-8'
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

version = "1.0"
base {
    archivesName = "MyModName"
}

minecraft {
    version = "1.12.2-14.23.5.2859"
    mappings = "stable_39"
    useDepAts = true
}
```

### Требования
- Gradle 4.9+ (рекомендуется 6.9)
- JDK 8 (для запуска игры и декомпиляции майнкрафт, setupDecompWorkspace) и JDK 17-21 для запуска gradle
