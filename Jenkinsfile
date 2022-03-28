pipeline {
  agent { label 'rhel7' }
  parameters {
    string(name: 'VERSION', defaultValue: '', description: 'Setting this field will create a tag and deploy the version')
  }
  stages {
    stage ("Checkout") {
      steps {
        git url: "https://github.com/${params.FORK}/quarkus-ls.git", branch: params.BRANCH
      }
    }
    stage ("Update version number") {
      when { not { equals expected: '', actual: VERSION } }
      steps {
        sh '''
          export JAVA_HOME="${NATIVE_TOOLS}${SEP}jdk11_last"
          MVN="${COMMON_TOOLS}${SEP}maven3-latest/bin/mvn -V -Dmaven.repo.local=${WORKSPACE}/.repository/ -B -ntp"
          ${MVN} -f ${WORKSPACE}/quarkus.jdt.ext/pom.xml org.eclipse.tycho:tycho-versions-plugin:1.7.0:set-version -DnewVersion=${VERSION} -Dtycho.mode=maven
          ${MVN} -f ${WORKSPACE}/quarkus.ls.ext/com.redhat.quarkus.ls/pom.xml versions:set -DnewVersion=$VERSION -DnewVersion=${VERSION} -Dtycho.mode=maven
          ${MVN} -f ${WORKSPACE}/qute.jdt/pom.xml org.eclipse.tycho:tycho-versions-plugin:1.7.0:set-version -DnewVersion=${VERSION} -Dtycho.mode=maven
          ${MVN} -f ${WORKSPACE}/qute.ls/com.redhat.qute.ls/pom.xml versions:set -DnewVersion=$VERSION -DnewVersion=${VERSION} -Dtycho.mode=maven
        '''
      }
    }
    stage ("Deploy to JBoss.org") {
      steps {
        sh '''
          WORKSPACE_SAV=${WORKSPACE}
          export JAVA_HOME="${NATIVE_TOOLS}${SEP}jdk11_last"
          MVN="${COMMON_TOOLS}${SEP}maven3-latest/bin/mvn -V -Dmaven.repo.local=${WORKSPACE}/.repository/ -B -ntp"
          BUILD_TIMESTAMP=`date -u +%Y-%m-%d_%H-%M-%S`
		
          pom=${WORKSPACE_SAV}/quarkus.jdt.ext/pom.xml

          pomVersion=$(grep "<version>" ${pom} | head -1 | sed -e "s#.*<version>\\(.\\+\\)</version>.*#\\1#")
          WORKSPACE=${WORKSPACE_SAV}/quarkus.jdt.ext/com.redhat.microprofile.jdt.quarkus.site/target/releng-scripts
          if [[ ${pomVersion} == *"-SNAPSHOT" ]]; then
            ${MVN} -f ${pom} -Pdeploy-to-jboss.org clean deploy -DJOB_NAME=${JOB_NAME} -DBUILD_NUMBER=${BUILD_NUMBER} -DBUILD_TIMESTAMP=${BUILD_TIMESTAMP} -DdeployTargetFolder=vscode/snapshots/builds/quarkus-jdt/${BUILD_TIMESTAMP}-B${BUILD_NUMBER}/all/repo/ -Dsurefire.timeout=1800 -Dmaven.test.failure.ignore=true
          else
            ${MVN} -f ${pom} -Pdeploy-to-jboss.org clean deploy -DJOB_NAME=${JOB_NAME} -DBUILD_NUMBER=${BUILD_NUMBER} -DBUILD_TIMESTAMP=${BUILD_TIMESTAMP} -DdeployTargetFolder=vscode/stable/builds/quarkus-jdt/${BUILD_TIMESTAMP}-B${BUILD_NUMBER}/all/repo/ -Dsurefire.timeout=1800 -Dmaven.test.failure.ignore=true -DdeployNumbuildstokeep=1000 -DdeployNumbuildstolink=1000
          fi

          pom=${WORKSPACE_SAV}/qute.jdt/pom.xml

          pomVersion=$(grep "<version>" ${pom} | head -1 | sed -e "s#.*<version>\\(.\\+\\)</version>.*#\\1#")
          WORKSPACE=${WORKSPACE_SAV}/qute.jdt/com.redhat.qute.jdt.site/target/releng-scripts
          if [[ ${pomVersion} == *"-SNAPSHOT" ]]; then
            ${MVN} -f ${pom} -Pdeploy-to-jboss.org clean deploy -DJOB_NAME=${JOB_NAME} -DBUILD_NUMBER=${BUILD_NUMBER} -DBUILD_TIMESTAMP=${BUILD_TIMESTAMP} -DdeployTargetFolder=vscode/snapshots/builds/qute-jdt/${BUILD_TIMESTAMP}-B${BUILD_NUMBER}/all/repo/ -Dsurefire.timeout=1800 -Dmaven.test.failure.ignore=true
          else
            ${MVN} -f ${pom} -Pdeploy-to-jboss.org clean deploy -DJOB_NAME=${JOB_NAME} -DBUILD_NUMBER=${BUILD_NUMBER} -DBUILD_TIMESTAMP=${BUILD_TIMESTAMP} -DdeployTargetFolder=vscode/stable/builds/qute-jdt/${BUILD_TIMESTAMP}-B${BUILD_NUMBER}/all/repo/ -Dsurefire.timeout=1800 -Dmaven.test.failure.ignore=true -DdeployNumbuildstokeep=1000 -DdeployNumbuildstolink=1000
          fi
        '''
      }
    }
    stage ("Deploy to Nexus") {
      steps {
        sh '''
          mvnFlags="-B -ntp -DaltSnapshotDeploymentRepository=origin-repository.jboss.org"
          export JAVA_HOME="${NATIVE_TOOLS}${SEP}jdk11_last"
          MVN="${COMMON_TOOLS}${SEP}maven3-latest/bin/mvn -V -Dmaven.repo.local=${WORKSPACE}/.repository/"

          pom=${WORKSPACE}/quarkus.ls.ext/com.redhat.quarkus.ls/pom.xml

          pomVersion=$(grep "<version>" ${pom} | head -1 | sed -e "s#.*<version>\\(.\\+\\)</version>.*#\\1#")
          if [[ ${pomVersion} == *"-SNAPSHOT" ]]; then
            ${MVN} clean deploy ${mvnFlags} -f ${pom}
          else
            ${MVN} clean deploy ${mvnFlags} -DskipRemoteStaging=true  -f ${pom} \
              -DstagingDescription="[${JOB_NAME} ${BUILD_TIMESTAMP} ${BUILD_NUMBER}] :: ${pomVersion} :: deploy to local"
            ${MVN} nexus-staging:deploy-staged -f ${pom} \
              -DstagingDescription="[${JOB_NAME} ${BUILD_TIMESTAMP} ${BUILD_NUMBER}] :: ${pomVersion} :: deploy to stage + close"
            ${MVN} nexus-staging:release -f ${pom} \
              -DstagingDescription="[${JOB_NAME} ${BUILD_TIMESTAMP} ${BUILD_NUMBER}] :: ${pomVersion} :: release"
          fi

          pom=${WORKSPACE}/qute.ls/com.redhat.qute.ls/pom.xml

          pomVersion=$(grep "<version>" ${pom} | head -1 | sed -e "s#.*<version>\\(.\\+\\)</version>.*#\\1#")
          if [[ ${pomVersion} == *"-SNAPSHOT" ]]; then
            ${MVN} clean deploy ${mvnFlags} -f ${pom}
          else
            ${MVN} clean deploy ${mvnFlags} -DskipRemoteStaging=true  -f ${pom} \
              -DstagingDescription="[${JOB_NAME} ${BUILD_TIMESTAMP} ${BUILD_NUMBER}] :: ${pomVersion} :: deploy to local"
            ${MVN} nexus-staging:deploy-staged -f ${pom} \
              -DstagingDescription="[${JOB_NAME} ${BUILD_TIMESTAMP} ${BUILD_NUMBER}] :: ${pomVersion} :: deploy to stage + close"
            ${MVN} nexus-staging:release -f ${pom} \
              -DstagingDescription="[${JOB_NAME} ${BUILD_TIMESTAMP} ${BUILD_NUMBER}] :: ${pomVersion} :: release"
          fi
        '''
      }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: '**/*.jar*', fingerprint: true
      deleteDir()
    }
  }
}
