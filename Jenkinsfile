@Library('my-shared-library') _

pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  namespace: jenkins
  labels:
    jenkins/agent-type: kaniko
spec:
  nodeSelector:
    nodetype: agent
  containers:
  - name: jnlp
    image: chauid/jenkins-inbound-agent:jdk17-k8s
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command:
      - /busybox/cat
    tty: true
    volumeMounts:
      - name: docker-secret
        mountPath: /kaniko/.docker
  volumes:
    - name: docker-secret
      secret:
        secretName: docker-config-postsmith-hub
            '''
        }
    }

    options {
        timeout(10)
    }

    stages {
        stage('Initialize environment') {
            steps {
                script {
                    env.STAGE_SEQUENCE = 0
                    env.IMAGE_NAME = 'postsmith-hub.kr.ncr.ntruss.com/postsmith-auth'
                    env.IMAGE_TAG = build.getProjectVersion('springboot')
                    withCredentials([file(credentialsId: 'postsmith_auth_application_properties', variable: 'APPLICATION_PROPERTIES')]) {
                        sh 'cp -f ${APPLICATION_PROPERTIES} ./src/main/resources/application.properties'
                    }
                }
            }
        }
        stage('Build gradle') {
            steps {
                script {
                    github.setCommitStatus("Building Spring Boot application", "CI / gradle build", "PENDING")
                    env.STAGE_SEQUENCE = 1
                    build.gradle()
                    github.setCommitStatus("Spring Boot application built successfully", "CI / gradle build", "SUCCESS")
                }
            }
        }
        stage('Build Container Image') {
            when {
                branch 'main';
            }
            steps {
                script {
                    github.setCommitStatus("Building Container image", "CI / Image Build", "PENDING")
                    env.STAGE_SEQUENCE = 2
                    build.image(env.IMAGE_NAME, env.IMAGE_TAG, true)
                    github.setCommitStatus("Container image built successfully", "CI / Image Build", "SUCCESS")
                }
            }
        }
        stage('Deploy K8s') {
            when {
                branch 'main';
            }
            steps {
                script {
                    github.setCommitStatus("Deploy to Kubernetes cluster", "CD / Kubernetes rollout", "PENDING")
                    env.STAGE_SEQUENCE = 3
                    k8s.deploy("postsmith-auth-app-deploy", "postsmith-auth-app", "postsmith-deploy", env.IMAGE_NAME, env.IMAGE_TAG)
                    github.setCommitStatus("Kubernetes cluster Deployed successfully", "CD / Kubernetes rollout", "SUCCESS")
                }
            }
        }

    }
    post {
        unsuccessful {
            script {
                switch (env.STAGE_SEQUENCE) {
                    case '0':
                        github.setCommitStatus("Failed to initialize the build process.", "Jenkins", "FAILURE")
                        break
                    case '1':
                        github.setCommitStatus("Failed to build the Spring Boot application.", "CI / gradle build", "FAILURE")
                        break
                    case '2':
                        github.setCommitStatus("Failed to build the Container image.", "CI / Image Build", "FAILURE")
                        break
                    case '3':
                        github.setCommitStatus("Failed to deploy to Kubernetes cluster.", "CD / Kubernetes rollout", "FAILURE")
                        break
                }
            }
        }
    }
}