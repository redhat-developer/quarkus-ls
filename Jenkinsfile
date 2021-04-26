pipeline {
  agent { label 'rhel7' }
  parameters {
    string(name: 'VERSION', defaultValue: '', description: 'Setting this field will create a tag and deploy the version')
  }
  stages {
    stage ("Update version number") {
      when { not { equals expected: '', actual: VERSION } }
      steps {
        sh '''
          export JAVA_HOME="${NATIVE_TOOLS}${SEP}jdk11_last"
          MVN="${COMMON_TOOLS}${SEP}maven3-latest/bin/mvn -V -Dmaven.repo.local=${WORKSPACE}/.repository/"
          ${MVN} -B -f ${WORKSPACE}/quarkus.jdt.ext/pom.xml org.eclipse.tycho:tycho-versions-plugin:1.7.0:set-version -DnewVersion=${VERSION}-SNAPSHOT -Dtycho.mode=maven
          ${MVN} -B -f ${WORKSPACE}/quarkus.ls.ext/com.redhat.quarkus.ls/pom.xml versions:set -DnewVersion=$VERSION -DnewVersion=${VERSION} -Dtycho.mode=maven
          git config --global user.email "tools@jboss.org"
          git config --global user.name "Quarkus LS GitHub Bot"
          git add -v '**/pom.xml' '**/MANIFEST.MF'
          git commit -s -m "Release ${VERSION}"
        '''
      }
    }
    stage ("Deploy to JBoss.org") {
      steps {
        sh '''
          pom=${WORKSPACE}/quarkus.jdt.ext/pom.xml

          pomVersion=$(grep "<version>" ${pom} | head -1 | sed -e "s#.*<version>\\(.\\+\\)</version>.*#\\1#")
          WORKSPACE=${WORKSPACE}/quarkus.jdt.ext/com.redhat.microprofile.jdt.quarkus.site/target/releng-scripts
          export JAVA_HOME="${NATIVE_TOOLS}${SEP}jdk11_last"
          MVN="${COMMON_TOOLS}${SEP}maven3-latest/bin/mvn -V -Dmaven.repo.local=${WORKSPACE}/.repository/"
          BUILD_TIMESTAMP=`date -u +%Y-%m-%d_%H-%M-%S`
          if [[ ${pomVersion} == *"-SNAPSHOT" ]]; then
            ${MVN} -B -f ${pom} -Pdeploy-to-jboss.org clean deploy -DJOB_NAME=${JOB_NAME} -DBUILD_NUMBER=${BUILD_NUMBER} -DBUILD_TIMESTAMP=${BUILD_TIMESTAMP} -DdeployTargetFolder=vscode/snapshots/builds/quarkus-jdt/${BUILD_TIMESTAMP}-B${BUILD_NUMBER}/all/repo/ -Dsurefire.timeout=1800
          else
            ${MVN} -B -f ${pom} -Pdeploy-to-jboss.org clean deploy -DJOB_NAME=${JOB_NAME} -DBUILD_NUMBER=${BUILD_NUMBER} -DBUILD_TIMESTAMP=${BUILD_TIMESTAMP} -DdeployTargetFolder=vscode/stable/builds/quarkus-jdt/${BUILD_TIMESTAMP}-B${BUILD_NUMBER}/all/repo/ -Dsurefire.timeout=1800
          fi
        '''
      }
    }
    stage ("Deploy to Nexus") {
      steps {
        sh '''
          pom=${WORKSPACE}/quarkus.ls.ext/com.redhat.quarkus.ls/pom.xml

          pomVersion=$(grep "<version>" ${pom} | head -1 | sed -e "s#.*<version>\\(.\\+\\)</version>.*#\\1#")
          mvnFlags="-B -DaltSnapshotDeploymentRepository=origin-repository.jboss.org"
          export JAVA_HOME="${NATIVE_TOOLS}${SEP}jdk11_last"
          MVN="${COMMON_TOOLS}${SEP}maven3-latest/bin/mvn -V -Dmaven.repo.local=${WORKSPACE}/.repository/"
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
    success {
      sh '''
        if [[ ${VERSION} != "" ]]; then
          git tag '${VERSION}'
          git push origin '${VERSION}'
        fi
      '''
    }
    always {
      archiveArtifacts artifacts: '**/*.jar*', fingerprint: true
      sh 'git push origin ${GIT_LOCAL_BRANCH}'
      deleteDir()
    }
  }
}
