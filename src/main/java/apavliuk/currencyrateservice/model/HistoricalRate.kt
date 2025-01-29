package apavliuk.currencyrateservice.model

import io.github.joselion.springr2dbcrelationships.annotations.ManyToOne
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table
class HistoricalRate (
    @Id
    val id: Long? = null,
    @ManyToOne val currency: Currency,
    val timestamp: Long,
    val rate: BigDecimal,
)