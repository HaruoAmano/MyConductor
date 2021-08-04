package music.elsystem.myconductor.gldraw

object Shader {
    //ドット描画用シェーダ************************************************************
    //バーテックスシェーダ
    val dotVertexSource =
        "uniform mat4 vpMatrix;" +
                "uniform mat4 wMatrix;" +
                "attribute vec3 position;" +
                "attribute vec4 dot_color;" +
                "varying vec4 f_dot_color;" +
                "void main() {" +
                "  f_dot_color = dot_color;" +
                "  gl_Position = vpMatrix * wMatrix * vec4(position, 1.0);" +
                "}"
    //フラグメントシェーダ
    val dotFagmentSource =
        "precision mediump float;" +
                "uniform vec4 color;" +
                "varying vec2 fuv;" +
                "varying vec4 f_dot_color;" +
                "void main() {" +
                "  gl_FragColor = color + f_dot_color;" +
                "}"
    //ライン描画用シェーダ************************************************************
    //バーテックスシェーダ
    val lineVertexSource =
        "uniform mat4 vpMatrix;" +
                "uniform mat4 wMatrix;" +
                "attribute vec3 position;" +
                "void main() {" +
                "  gl_Position = vpMatrix * wMatrix * vec4(position, 1.0);" +
//                "  gl_PointSize = 10.0f;" +
                "}"
    //フラグメントシェーダ
    val lineFragmentSource = "precision mediump float;" +
            "void main() {" +
            "  gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);" +
            "}"
    //ナンバー描画用シェーダ************************************************************
    //バーテックスシェーダ
    val numberVertexSource =
        "uniform mat4 vpMatrix;" +
                "uniform mat4 wMatrix;" +
                "attribute vec3 position;" +
                "attribute vec2 uv;" +
                "varying vec2 fuv;" +
                "void main() {" +
                "  fuv = uv;" +
                "  gl_Position = vpMatrix * wMatrix * vec4(position, 1.0);" +
                "}"
    //フラグメントシェーダ
    val numberFragmentSource =
        "precision mediump float;" +
                "uniform vec4 color;" +
                "varying vec2 fuv;" +
                "uniform sampler2D texture;" +
                "void main() {" +
                "  gl_FragColor = color * texture2D(texture, fuv);" +
                "}"
}