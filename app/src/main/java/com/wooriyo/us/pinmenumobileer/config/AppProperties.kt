package com.wooriyo.us.pinmenumobileer.config

class AppProperties {
        companion object {
                private const val REAL_SERVER: String = "http://app.pinmenu.biz/api/"
                private const val TEST_SERVER: String = "http://testapp.pinmenu.biz/api/"
                private const val REAL_IMG_SERVER: String = "http://img.pinmenu.biz/api/"
                private const val AWS_SERVER: String = "http://app.pinmenu.net/api/"
                private const val AWS_IMG_SERVER: String = "http://img.pinmenu.net/api/"

                const val SERVER: String = AWS_SERVER
                const val IMG_SERVER: String = AWS_IMG_SERVER

                // 푸시 알림 아이디
                const val NOTIFICATION_ID_ORDER = 1
                const val NOTIFICATION_ID_CALL = 2

                // 푸시 알림 채널
                const val CHANNEL_ID_ORDER = "pinmenuer_mobile_order"
                const val CHANNEL_ID_CALL = "pinmenuer_mobile_call"

                // 리사이클러뷰 멀티뷰 사용시 타입
                const val VIEW_TYPE_COM = 0
                const val VIEW_TYPE_ADD = -1
                const val VIEW_TYPE_EMPTY = -2

                const val VIEW_TYPE_ORDER = 3
                const val VIEW_TYPE_CALL = 4
                const val VIEW_TYPE_RESERVATION = 5

                // Activity Result Code
                const val RESULT_DELETE = 10
                const val RESULT_MODIFY = 11

                // 권한 관련
                const val REQUEST_LOCATION = 0
                const val REQUEST_ENABLE_BT = 1
                const val REQUEST_NOTIFICATION = 2
                const val REQUEST_STORAGE = 3

                //프린트 관련

                /** RT 붙은 변수 > 미국판 프린터(RP325)용 변수 **/
                const val RT_ONE_LINE_SMALL = 48   // 작은 글자 (default) 한 줄 개수 - 공백, 하이픈, 소문자, 대문자 모두 동일)
                const val RT_ONE_LINE_BIG = 23     // 큰 글자 (넓이 두배) 한 줄 개수 - 공백, 하이픈, 소문자, 대문자 모두 동일)

                const val RT_PRODUCT_BIG = 19      // 큰 글자 기준, 상품명 칸에 들어갈 글자 개수
                const val RT_QTY_BIG = 3           // 큰 글자 기준, 수량 칸에 들어갈 글자 개수
                const val RT_AMT_BIG = 6           // 큰 글자 기준, 가격 칸에 들어갈 글자 개수 (vvAMTv) (000.00) (#00.00) (##0.00)

                const val RT_PRODUCT_SMALL = 43      // 큰 글자 기준, 상품명 칸에 들어갈 글자 개수
                const val RT_QTY_SMALL = 3           // 큰 글자 기준, 수량 칸에 들어갈 글자 개수
                const val RT_AMT_SMALL = 6           // 큰 글자 기준, 가격 칸에 들어갈 글자 개수 (vvAMTv) (000.00) (#00.00) (##0.00)

                const val FONT_BIG = 37
                const val FONT_SMALL = 28

                const val FONT_SIZE = FONT_BIG

                const val FONT_WIDTH = 512

                const val HANGUL_SIZE_BIG = 3.8
                const val HANGUL_SIZE_SMALL = 3.5

                const val HANGUL_SIZE = HANGUL_SIZE_BIG

                const val ENG_SIZE_UPPER_BIG = 2.53
                const val ENG_SIZE_LOWER_BIG= 1.9
                const val ENG_SIZE_UPPER_SMALL = 2.4
                const val ENG_SIZE_LOWER_SMALL = 1.8

                const val ENG_SIZE_UPPER = ENG_SIZE_UPPER_BIG
                const val ENG_SIZE_LOWER= ENG_SIZE_LOWER_BIG

                const val NUM_SIZE_BIG= 2.3
                const val NUM_SIZE_SMALL= 1.8

                const val NUM_SIZE= NUM_SIZE_BIG

                const val ONE_LINE_BIG = 38
                const val ONE_LINE_SMALL = 53

                const val HYPHEN_NUM_BIG = 50
                const val HYPHEN_NUM_SMALL = 66

                const val HYPHEN_NUM = HYPHEN_NUM_BIG

                const val SPACE_BIG = 3
                const val SPACE_SMALL = 5

                const val SPACE = SPACE_BIG

                const val TITLE_MENU = "메     뉴     명                          수량         금액  "
        }
}