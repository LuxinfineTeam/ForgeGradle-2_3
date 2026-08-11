# ForgeGradle 2.3 extended
Это форк проекта https://github.com/anatawa12/ForgeGradle-2.3/

### Список изменений:
- Поддержка запуска gradle на JDK 17-21
- Использование apache compress для замены удаленного Pack200 класса из новых версий Java
- Небольшие оптимизации общей работы плагина за счет перехода на java nio в некоторых местах
- Поддержка автоматического применения _at.cfg из всех зависимостей проекта при setupDecompWorkspace. При необходимости можно отключить указав useAtFromDependencies=false в minecraft конфигурации
- Подготовка к поддержке современных версий Gradle (9.0+) через ExecHelper

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
        classpath('com.github.LuxinfineTeam.ForgeGradle-2_3:ForgeGradle:main-SNAPSHOT') {
            changing = true
        }
    }
}

apply plugin: 'net.minecraftforge.gradle.forge'

[compileJava, compileTestJava]*.options*.encoding = 'UTF-8'
sourceCompatibility = '1.8'
targetCompatibility = '1.8'

version = "1.0"
archivesBaseName = "MyModName"

minecraft {
    version = "1.12.2-14.23.5.2859"
    mappings = "stable_39"
    useDepAts = true
}

// Добавляем флаг компилятора, чтобы итоговый билд работал на java8
tasks.withType(JavaCompile) {
    if (JavaVersion.current().isJava9Compatible()) {
        options.compilerArgs.addAll(['--release', '8'])
    }
}
```

### Требования
- Gradle 4.9+ (рекомендуется 6.9)
- JDK 8 (для запуска игры) и JDK 17-21 для запуска gradle
