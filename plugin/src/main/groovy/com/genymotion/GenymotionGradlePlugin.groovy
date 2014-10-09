package main.groovy.com.genymotion

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.UnknownTaskException


class GenymotionGradlePlugin implements Plugin<Project> {

    static final String PLUGIN_GROUP = "Genymotion"
    static final String TASK_LAUNCH = "genymotionLaunch"
    static final String TASK_FINISH = "genymotionFinish"

    void apply(Project project) {

        println "adding Genymotion plugin"
        project.extensions.create('genymotion', GenymotionPluginExtension, project);
        project.genymotion.extensions.create('config', GenymotionConfig); //the extension name have to be different from the original nested element's name (receiver)
        project.genymotion.extensions.create('admin', GenymotionAdmin); //the extension name have to be different from the original nested element's name (receiver)
        project.genymotion.extensions.create('devices', GenymotionDevices); //the extension name have to be different from the original nested element's name (receiver)

        project.task(TASK_LAUNCH, type: GenymotionTask){
            description 'Starting task for Genymotion plugin'
            group PLUGIN_GROUP
        }
        project.task(TASK_FINISH, type: GenymotionEndTask){
            description 'Finishing task for Genymotion plugin'
            group PLUGIN_GROUP
        }

        //we set the config inside the GenymotionTool
        GenymotionTool.GENYMOTION_CONFIG = project.genymotion.config

        project.afterEvaluate {

            GenymotionPluginExtension.checkParams()

            def taskLaunch = project.genymotion.config.taskLaunch

            try{
                //if the automatic launch is enable and the configuration is correct
                if (project.genymotion.config.automaticLaunch && taskLaunch != null &&
                   (project.plugins.hasPlugin('android') || taskLaunch != GenymotionConfig.DEFAULT_ANDROID_TASK)
                ) {

                    def theTask = project.tasks.getByName(taskLaunch)
                    println "Adding genymotion dependency to " + taskLaunch
                    theTask.dependsOn(TASK_LAUNCH)
                    theTask.finalizedBy(TASK_FINISH)
                }

            } catch (UnknownTaskException e){
                println "Task $taskLaunch not found. genymotionLaunch/Finish tasks are not injected and has to be launched manually."
            }

        }
    }
}