package com.huifu.virgo.common.base;

public class EnumUtils {

    public static enum DispatherErrorType {

        /** 系统错误，需要入库 */
        VirgoException(0), /** transform to testMessage 错误 */
        Transform(1), /** Json value parse 错误 */
        JsonParse(2), /** Json value mapping 错误! */
        JsonMapping(3), /** Json value generation 错误 */
        JsonGeneration(4), /** Io 错误 */
        Io(5);
        private int code;

        private DispatherErrorType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

}
