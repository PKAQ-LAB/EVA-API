package org.pkaq.helper.seq.seq;

import lombok.RequiredArgsConstructor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class SequenceGenerator {

    private final SequenceRepository sequenceRepository;

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 支持的函数和常量
     * 前缀函数：#prefix('PREFIX')
     * 用于添加固定的前缀。
     * 示例：#prefix('ORD') 生成 ORD
     * 日期函数：#date('PATTERN')
     * 用于添加格式化的当前日期。
     * 支持的日期模式与 java.time.format.DateTimeFormatter 相同。
     * 示例：#date('yyMM') 在 2024 年 5 月生成 2405
     * 序列号函数：#seq(START, LENGTH, 'RESET_PERIOD')
     * 用于生成递增的序列号。
     * 参数说明：
     * START：起始序列号
     * LENGTH：序列号长度，不足长度时补零
     * RESET_PERIOD：重置周期，可选值 DAILY、MONTHLY、YEARLY、YY，分别表示按日、月、年和两位数年份重置
     * 示例：#seq(1, 5, 'MONTHLY') 生成 00001
     *  规则示例1：#prefix('ORD') + '-' + #date('yyMM') + '-' + #seq(1,5,'MONTHLY')
     *  结果示例：ORD-2405-00001
     *  规则示例2：#date('yy') + #seq(1,4,'YY')
     *  结果示例：24-0001
     *  规则示例3：#seq(1,6,'DAILY')
     */

    public String generateSeqNum(String tenantId, String rule) {
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("s", new RuleFunction(tenantId, sequenceRepository));
        return parser.parseExpression(rule).getValue(context, String.class);
    }
}
