package com.atob.ch7

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private val soundVisualizerView: SoundVisualizerView by lazy {
        findViewById(R.id.soundVisualizerView)
    }
    // time TextView
    private val recordTimeTextView: CountUpView by lazy {
        findViewById(R.id.recordTimeTextView)
    }

    // 리셋 버튼
    private val resetButton:Button by lazy {
        findViewById(R.id.restButton)
    }
    // 정지, 시작 버튼
    private val recordButton: RecordButton by lazy {
        findViewById(R.id.recordButton)
    }

    // 권한 요청할 목록
    private val requiredPermissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)

    // 시작, 정지 버튼 변경(변화되는 상태 값)
    private var state = State.BEFORE_RECORDING
        set(value) {
            field = value
            // 녹음 중이거나 녹음한 파일을 플레이할 땐 버튼 활성화
            resetButton.isEnabled = (value == State.AFTER_RECORDING) ||
                    (value==State.ON_PLAYING)
            recordButton.updateIconWithState(field)
        }

    // 녹음기
    private var recorder: MediaRecorder? = null

    // 외부 저장소 경로
    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 권한 허용
        requestAudioPermission()
        // 초기 View
        initView()
        // 화면 버튼
        bindViews()
        // 초기 state
        initVariables()
    }

    private fun bindViews() {
        soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude?:0
        }

        resetButton.setOnClickListener {
            stopPlaying()       // 재생중에 리셋을 누를 수 있기 때문에
            soundVisualizerView.clearVisualization()
            recordTimeTextView.clearCountTime()
            state = State.BEFORE_RECORDING
        }

        recordButton.setOnClickListener {
            when (state) {
                State.BEFORE_RECORDING -> startRecording()
                State.ON_RECORDING -> stopRecording()
                State.AFTER_RECORDING -> startPlaying()
                State.ON_PLAYING -> stopPlaying()
            }
        }
    }

    // 권한 결과값
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                    grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (!audioRecordPermissionGranted) {
            finish()
        }
    }

    // 권한 허용 요청
    private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initView() {
        // 처음엔 정지 상태가 보여야함.
        recordButton.updateIconWithState(state)
    }

    // 녹음 시작
    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)
            prepare()
        }
        recorder?.start()
        soundVisualizerView.startVisualizing(false)
        recordTimeTextView.startCountUp()
        state = State.ON_RECORDING
    }

    // 녹음 정지
    private fun stopRecording() {
        recorder?.run {
            stop()
            release()
        }
        // null 처리를 해줌으로써 메모리 차지하는 것을 막아준다.
        recorder = null
        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        state = State.AFTER_RECORDING
    }

    // 녹음한 파일 시작
    private fun startPlaying() {
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECORDING
        }
        player?.start()
        soundVisualizerView.startVisualizing(true)
        recordTimeTextView.startCountUp()
        state = State.ON_PLAYING
    }

    // 녹음한 파일 정지
    private fun stopPlaying() {
        player?.release()
        player = null
        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        state = State.AFTER_RECORDING
    }

    // 초기 state
    private fun initVariables(){
        state = State.BEFORE_RECORDING
    }

    // 권한 ID
    companion object {
        val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }


}
