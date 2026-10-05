package com.example.unum.domain.books

import com.example.unum.data.model.*
import com.example.unum.domain.usecase.PremiumMonthPlanner

/** Book ordering, conflict resolution and stored reading rules; no UI or IO. */
object FortuneBookPolicy {
    fun mergeBooks(localBooks: List<FortuneBook>, remoteBooks: List<FortuneBook>): List<FortuneBook> {
        val merged = linkedMapOf<String, FortuneBook>()
        (remoteBooks + localBooks).forEach { book ->
            val current = merged[book.bookId]
            if (current == null || (book.lastOpenedAt ?: book.createdAt) >= (current.lastOpenedAt ?: current.createdAt)) {
                merged[book.bookId] = book
            }
        }
        return sortBooks(merged.values.toList())
    }

    fun refreshStoredMonthInsights(book: FortuneBook): FortuneBook {
        if (book.bookType != FortuneBookType.PERSONAL) return book
        val topic = PremiumMonthPlanner.topicFromThemeOrLabel(book.coverTheme, book.concernTopic) ?: return book
        val currentMonth = PremiumMonthPlanner.currentMonth()
        val bookNumbers = NumerologyNumbers(
            destiny = book.destiny,
            early = book.early,
            middle = book.middle,
            late = book.late,
            code = book.code
        )
        val bestSelection = PremiumMonthPlanner.pickBestMonth(topic, bookNumbers, currentMonth)
        val riskySelection = PremiumMonthPlanner.pickRiskyMonth(topic, bookNumbers, currentMonth)
        val bestMonth = bestSelection.toDisplayText()
        val riskyMonth = riskySelection.toDisplayText()
        val shouldRefreshBest = book.bestMonth != bestMonth ||
            PremiumMonthPlanner.isPastMonthText(book.bestMonth, currentMonth)
        val shouldRefreshRisky = book.riskyMonth != riskyMonth ||
            PremiumMonthPlanner.isPastMonthText(book.riskyMonth, currentMonth)

        if (!shouldRefreshBest && !shouldRefreshRisky) return book

        return book.copy(
            bestMonth = if (shouldRefreshBest) bestMonth else book.bestMonth,
            bestMonthReason = if (shouldRefreshBest) buildStoredBestMonthReason(topic, bestSelection) else book.bestMonthReason,
            riskyMonth = if (shouldRefreshRisky) riskyMonth else book.riskyMonth,
            riskyMonthReason = if (shouldRefreshRisky) buildStoredRiskyMonthReason(topic, riskySelection) else book.riskyMonthReason
        )
    }

    private fun buildStoredBestMonthReason(
        topic: PremiumTopic,
        selection: PremiumMonthPlanner.MonthSelection
    ): String {
        val monthText = selection.toDisplayText()
        val base = when (topic) {
            PremiumTopic.ROMANCE -> "${monthText}에는 마음을 편안하게 열고 관계의 온도를 다시 맞추기 좋습니다. 부담스러운 확인보다 구체적인 만남 제안이 자연스럽게 이어집니다."
            PremiumTopic.CAREER -> "${monthText}에는 준비한 내용을 지원, 제안, 면접으로 옮기기 좋습니다. 조건과 역할을 선명하게 정리하면 기회가 더 분명해집니다."
            PremiumTopic.MONEY -> "${monthText}에는 수입과 지출 구조를 다시 세우기 좋습니다. 큰 결정보다 기준을 세우는 행동이 돈의 흐름을 안정시킵니다."
            PremiumTopic.STUDY -> "${monthText}에는 집중 범위를 정리하고 취약 영역을 보완하기 좋습니다. 반복 학습이 시험과 과제의 실수를 줄여줍니다."
            PremiumTopic.HEALTH -> "${monthText}에는 수면과 활동 리듬을 회복하기 좋습니다. 무리한 변화보다 꾸준한 생활 시간이 컨디션을 안정시킵니다."
            PremiumTopic.BUSINESS -> "${monthText}에는 거래 조건과 수익 구조를 점검하기 좋습니다. 제안과 계약의 책임 범위를 분명히 하면 기회가 안정적으로 이어집니다."
            PremiumTopic.GENERAL -> "${monthText}에는 일, 돈, 관계의 우선순위가 선명해집니다. 가장 영향이 큰 한 영역에 힘을 모으기 좋습니다."
            PremiumTopic.SELF_ESTEEM -> "${monthText}에는 스스로를 다시 세우는 힘이 살아납니다. 작은 약속을 지키는 경험이 마음의 중심을 단단하게 합니다."
            PremiumTopic.RELATIONSHIP -> "${monthText}에는 사람들과의 접점이 자연스럽게 열립니다. 미뤄둔 대화나 관계 회복을 부드럽게 시작하기 좋습니다."
        }
        val passedMonth = selection.replacedPastMonth ?: return base
        return "올해 가장 추천 흐름이 강했던 ${passedMonth}월은 이미 지났습니다. 지금 이후에는 ${monthText}을 다음 추천 구간으로 보세요. $base"
    }

    private fun buildStoredRiskyMonthReason(
        topic: PremiumTopic,
        selection: PremiumMonthPlanner.MonthSelection
    ): String {
        val monthText = selection.toDisplayText()
        val base = when (topic) {
            PremiumTopic.ROMANCE -> "${monthText}에는 마음이 앞서 결론을 재촉하기 쉽습니다. 상대의 반응 속도와 여백을 함께 살피는 편이 안전합니다."
            PremiumTopic.CAREER -> "${monthText}에는 변화 욕구가 커져 성급한 결정을 내리기 쉽습니다. 큰 선택은 조건을 다시 확인한 뒤 움직이는 편이 안전합니다."
            PremiumTopic.MONEY -> "${monthText}에는 빠른 이익을 좇고 싶은 마음이 커질 수 있습니다. 확인되지 않은 제안과 충동 지출은 거리를 두는 편이 안전합니다."
            PremiumTopic.STUDY -> "${monthText}에는 불안 때문에 계획만 늘리거나 밤샘으로 밀어붙이기 쉽습니다. 범위를 줄이고 수면을 지키는 편이 안전합니다."
            PremiumTopic.HEALTH -> "${monthText}에는 피로 신호를 무시하기 쉽습니다. 불편함이 지속되면 운세보다 의료진의 진료를 우선하세요."
            PremiumTopic.BUSINESS -> "${monthText}에는 확장 욕구가 커져 비용과 계약 조건을 낙관적으로 보기 쉽습니다. 문서와 숫자를 확인하기 전에는 큰 결정을 미루는 편이 안전합니다."
            PremiumTopic.GENERAL -> "${monthText}에는 여러 문제를 한 번에 해결하려다 판단이 흐려질 수 있습니다. 큰 결정을 겹치지 않는 편이 안전합니다."
            PremiumTopic.SELF_ESTEEM -> "${monthText}에는 비교와 조급함이 커지기 쉽습니다. 몸과 마음의 리듬을 먼저 회복하는 데 집중하세요."
            PremiumTopic.RELATIONSHIP -> "${monthText}에는 오해가 빠르게 번질 수 있습니다. 중요한 대화는 단정하지 말고 시간을 두는 편이 좋습니다."
        }
        val passedMonth = selection.replacedPastMonth ?: return base
        return if (selection.isNextYear) {
            "올해 가장 강하게 조심할 달인 ${passedMonth}월은 이미 지났습니다. 다음 해 ${selection.month}월을 다음 주의 구간으로 봅니다. $base"
        } else {
            "올해 가장 강하게 조심할 달인 ${passedMonth}월은 이미 지났습니다. 지금 이후에는 ${monthText}을 다음 주의 구간으로 보세요. $base"
        }
    }

    fun sortBooks(books: List<FortuneBook>): List<FortuneBook> {
        return books.sortedWith(
            compareByDescending<FortuneBook> { it.lastOpenedAt ?: it.createdAt }
                .thenByDescending { it.createdAt }
        )
    }

}
