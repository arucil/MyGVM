package plodsoft.mygvm.util


/**
 * @return 若a <= b, 返回CharRange(a, b), 否则返回CharRange(b, a)
 */
inline fun between(a: Char, b: Char): CharRange =
    if (a <= b) a..b else b..a

/**
 * @return 若a <= b, 返回IntRange(a, b), 否则返回IntRange(b, a)
 */
inline fun between(a: Int, b: Int): IntRange =
    if (a <= b) a..b else b..a

/**
 * @return 若a <= b, 返回LongRange(a, b), 否则返回LongRange(b, a)
 */
inline fun between(a: Long, b: Long): LongRange =
        if (a <= b) a..b else b..a
