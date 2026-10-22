
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 40, unit: 'MINUTES')
    }

      environment {
        BACKEND_IMAGE = "hostelhub-backend:candidate-${BUILD_NUMBER}"
        FRONTEND_IMAGE = "hostelhub-frontend:candidate-${BUILD_NUMBER}"
    }

    stages {        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/hostelhubsuperadmin-cloud/hms.git'
            }
        }

        stage('Build Backend Image') {
            steps {
                sh 'docker build -t "$BACKEND_IMAGE" ./hostelhub_backend'
            }
        }

        stage('Build Frontend Image') {
            steps {
                sh 'docker build -t "$FRONTEND_IMAGE" ./hms_frontend'
            }
        }
    }

    post {
        success {
            echo 'Both Docker images built successfully. Live containers were not changed.'
        }
        failure {
            echo 'Pipeline failed. Review the build logs; deployment was not attempted.'
        }
    }
}
