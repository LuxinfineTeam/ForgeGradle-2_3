/*
 * A Gradle plugin for the creation of Minecraft mods and MinecraftForge plugins.
 * Copyright (C) 2013-2019 Minecraft Forge
 * Copyright (C) 2020-2023 anatawa12 and other contributors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package net.minecraftforge.gradle.user;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

@DisableCachingByDefault(because = "Legacy access transformer extraction task")
public class TaskExtractDepAts extends DefaultTask
{
    @Input
    private List<String> configurations = Lists.newArrayList();
    @OutputDirectory
    private Object               outputDir;

    @TaskAction
    public void doTask() throws IOException
    {
        FileCollection col = getCollections();
        File outputDir = getOutputDir();
        outputDir.mkdirs(); // make sur eit exists

        // make a list of things to delete...
        List<File> toDelete = Lists.newArrayList(outputDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f)
            {
                return f.isFile();
            }
        }));

        for (File f : col)
        {
            if (!f.exists() || !f.getName().endsWith("jar"))
                continue;

            try (JarFile jar = new JarFile(f))
            {
                Enumeration<? extends ZipEntry> entries = jar.entries();

                //Пересматриваем ВЕСЬ jar, а не только то, что в его манифесте
                //Некоторые моды загружают _at.cfg программно, например ThermalMods, что ломает логику с проверкой манифеста
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    if (entryName.toLowerCase().endsWith("_at.cfg")) {
                        String fileName = entryName.substring(0, entryName.length() - "_at.cfg".length());
                        int lastSep = fileName.lastIndexOf('/');
                        if (lastSep != -1)
                            fileName = fileName.substring(lastSep + 1);

                        File outFile = new File(outputDir, fileName + "_" + Files.getNameWithoutExtension(f.getName()) + "_at.cfg");
                        toDelete.remove(outFile);

                        try (InputStream is = jar.getInputStream(entry)) {
                            java.nio.file.Files.copy(is, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }

        // remove the files that shouldnt be there...
        for (File f : toDelete)
        {
            f.delete();
        }
    }

    public List<String> getConfigurations() {
        return configurations;
    }

    @InputFiles
    @Classpath
    public FileCollection getCollections()
    {
    	List<Configuration> configs = Lists.newArrayListWithCapacity(configurations.size());
    	for (String s : configurations)
    		configs.add(getProject().getConfigurations().getByName(s));
        return getProject().files(configs);
    }

    public void addCollection(String col)
    {
        configurations.add(col);
    }

    public File getOutputDir()
    {
        return getProject().file(outputDir);
    }

    public void setOutputDir(Object outputFile)
    {
        this.outputDir = outputFile;
    }
}
