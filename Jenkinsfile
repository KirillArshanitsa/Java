
def getErrorMsg(errorText){
	ansiColor('xterm') {
		echo "\u001B[31m${errorText}\u001B[m"
		sh "exit 1"
	}		
}
def getSuccesMsg(sucText){
	ansiColor('xterm') {
		echo "\u001B[32m${sucText}\u001B[m"
	}		
}


//TODO заменить подстановку экранирования строки на переменные
def getAwrSnapshots(passFile, srvName, allAwrSnapFile){
	def myscript = "java -cp GetOracleAwrReport.jar WorkDbAwr.GetAllSnap ${srvName} ${passFile} ${allAwrSnapFile}"
	sh (script: myscript)
	resultData = []
	readingFile = readFile allAwrSnapFile
	myFile = readingFile.split("\n")
	myFile.each {
		line -> resultData.add(line.toString())
	}
	if (resultData.size() == 0){
		getErrorMsg("Ошибка - для базы ${DbName} не удалось получить Awr Snapshots")
	}
	return resultData
}

def sendEmail(awrReporFileName, beginSnapFullInfo, endSnapFullInfo){
	//TODO добавить проверку email через регрулярки
	echo "Отправка почты на адресс - ${env.emailAdress}"
	subject = "AWR c БД ${env.DbName}"
	body = "<p> AWR report c БД ${env.DbName} между SNAPSHOTами - <span style=\'color:#ff0000\'><b>${beginSnapFullInfo}</b></span> и <span style=\'color:#ff0000\'><b>${endSnapFullInfo}</b></span> <br>URL задания - ${env.BUILD_URL} </p>"
	emailext attachLog: false, attachmentsPattern: awrReporFileName,body: body, compressLog: false, mimeType: 'text/html', replyTo: '', subject: subject, to: env.emailAdress
}

def getDbForJobUpdate(tempConfFile){
	readedInfo = readFile(tempConfFile).split("\n")
	srvData = []
	for (str in readedInfo){
		srvData.add(str.split("=")[0])
	}
	if (srvData.size() == 0){
		getErrorMsg("Ошибка - не удалось получить сервера бд, для обновления параметров Job")
	}
	return srvData
}	

node('masterLin'){
	def encryptFile = "passwords.txt" //имя файла с шифрованными значениями
	def credential = "vault_cred" //имя кредов для ansible-vault
	def awrReporFileName = "awr.html" //имя итогового файла AWR отчёта
	def allAwrSnapshotsFile = "./allAwrSnapshots.txt" //имя файла куда пишутся все AWR snap для выбора из меню в pipeline
	def allDbSrvNames
	def dbPassInfo
	def awrShapshots
	def beginAwrSnap
	def endAwrSnap
	def beginAwrSnapFullInfo
	def endAwrSnapFullInfo
	stage('Работа с паролями'){
		if (!env.emailAdress){
			getErrorMsg("Ошибка - вы указали пустой праметр emailAdress ${env.emailAdress}, отправка почты невозможна!")		
		}			
		cleanWs()
		git 'https://url/getawr.git'
		echo "Start decrypt"		
		try{
			withCredentials([file(credentialsId: credential, variable: 'key_file')]) {
				ansible_tool = tool(name: 'ansible24', type: 'org.jenkinsci.plugins.ansible.AnsibleInstallation')
				def resultCrypto = sh (script:"${ansible_tool}/ansible-vault decrypt ${encryptFile} --vault-password-file=${key_file.toString()}", returnStatus: true)
				if (resultCrypto != 0){
					getErrorMsg("Ошибка при работе с шифрованными данными!")
				}
				else {
					getSuccesMsg("Работа с шифрованными данными выполнена успешно.")
				}					
			}
		} catch (Ex) {
			getErrorMsg(Ex.toString())
		}
		echo "Stop decrypt"		
		awrShapshots = getAwrSnapshots(encryptFile, env.DbName, allAwrSnapshotsFile)
		allDbSrvNames = getDbForJobUpdate(encryptFile)
		echo "Update Job Params"
		properties([parameters([
				choice(choices: allDbSrvNames, description: 'Выберите б.д.', name: 'DbName'),
				string(defaultValue: '', description: "<p>Укажите email, куда будет отправлен AWR Report. Сообщение приходит от <span style='color:#ff0000'><b>СБТ ДК УАТС ОПИР CI технологическая</b></span> , с темой - <b>AWR c БД ...</b></p>", name: 'emailAdress', trim: true),])
				]
			)
	}
    stage('Выбор AWR SNAPSHOTов'){
		timeout(4) {		
			result = input message: "Выберите номера SNAPSHOTов, beginAwrSnap должен быть меньше и не равен endAwrSnap!", parameters: [
				choice(choices: awrShapshots, description: 'Выберите начальный SNAPSHOT', name: 'beginAwrSnap'),
				choice(choices: awrShapshots, description: 'Выберите финальный SNAPSHOT', name: 'endAwrSnap')			
			]
		}
		beginAwrSnapFullInfo = result.beginAwrSnap
		endAwrSnapFullInfo = result.endAwrSnap
		beginAwrSnap = beginAwrSnapFullInfo.split(" ")[0]
		endAwrSnap =  endAwrSnapFullInfo.split(" ")[0]
		if (beginAwrSnap >= endAwrSnap){
			getErrorMsg("Ошибка - начальный AWR SNAPSHOT должен быть меньше и не равен финальному, вы выбрали начальным - ${beginAwrSnap}, финальным - ${endAwrSnap}")	
		}
		getSuccesMsg("Выбраны: начальный AWR SNAPSHOT - ${beginAwrSnap}, финальный AWR SNAPSHOT - ${endAwrSnap}")
	
	}
	stage('Выполнение'){
		def executedScript = "java -jar GetOracleAwrReport.jar " + env.DbName  + " " + encryptFile + " " + beginAwrSnap + " " + endAwrSnap + " " + awrReporFileName
		echo "Выполняется команда - " + executedScript
		sh (script: executedScript)		
	}	
	stage('Отправка email'){
		sendEmail(awrReporFileName, beginAwrSnapFullInfo, endAwrSnapFullInfo)
	}	
}	


