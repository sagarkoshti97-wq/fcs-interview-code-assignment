# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
### Key Challenges & Technical Considerations
1. **Direct vs. Shared/Indirect Cost Attribution:**
   - Direct costs (e.g., warehouse-specific labor or storage cost per SKU) are straightforward to attribute to a single `businessUnitCode`.
   - Shared costs (e.g., multi-stop transportation routes serving 3 `Stores` from 2 `Warehouses`, regional management overhead, or shared fleet maintenance) require Activity-Based Costing (ABC) allocation keys such as pallet volume, weight, distance traveled, or order line count.
2. **Dynamic Fulfillment Topology:**
   - Because a `Product` in a `Store` can be fulfilled by up to 2 `Warehouses` and a `Store` can be served by up to 3 `Warehouses`, cost allocation must be tracked at the **Fulfillment Association / Shipment event level** rather than static monthly averages.
3. **Granularity vs. System Overhead:**
   - Capturing real-time cost events per unit moved requires an event-driven ledger (immutable cost journal entries) that aggregates cleanly for analytical reporting without slowing down operational warehouse transactions.

### Discovery Questions for Stakeholders
- What are the agreed cost-allocation drivers for shared transportation and overhead (e.g., per unit shipped, cubic volume, capacity utilization, or store revenue share)?
- How frequently do finance and operations need cost rollups (real-time operational dashboards vs. end-of-month financial close)?
- How should reverse logistics (returns, damaged stock write-offs) be split between the originating `Warehouse` and the retail `Store`?

---

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
### Potential Cost Optimization Strategies & Expected Outcomes
1. **Capacity Utilization & Colocation Consolidation:**
   - *Strategy:* Analyze warehouses operating far below their `capacity` (e.g., `MWH.001` with `capacity = 100` and `stock = 10`) and either consolidate stock or replace oversized warehouses with right-sized facilities in the same `Location`.
   - *Expected Outcome:* 15–25% reduction in fixed facility overhead and utility expenses per unit stored.
2. **Proximity-Based Store-Warehouse Assignment:**
   - *Strategy:* Optimize the `Warehouse -> Store` fulfillment associations by prioritizing warehouses in the closest geographical `Location` for high-velocity products while respecting the 2-warehouse-per-product and 3-warehouse-per-store constraints.
   - *Expected Outcome:* Lower last-mile transportation fuel/fleet costs and shorter replenishment lead times.
3. **SKU Slotting & Inventory Balancing:**
   - *Strategy:*Since each `Warehouse` stores at most 5 `Product` types, group co-ordered products into the same warehouse to minimize split shipments to stores.
   - *Expected Outcome:* Higher truck fill rates and fewer partial deliveries.

### Identification, Prioritization, and Implementation Framework
- **Identify:** Build unit-economics metrics (`Cost per Order Fulfilled`, `Cost per Unit Stored`, `Capacity Utilization %` = `stock / capacity`).
- **Prioritize:** Rank initiatives on an **Impact vs. Implementation Effort** matrix, protecting SLA metrics (store stockout rate, replenishment lead time).
- **Implement:** Roll out changes iteratively (pilot in one location like `AMSTERDAM-001` before network-wide rollout) and measure before/after cost KPIs.

### Discovery Questions for Stakeholders
- What are the non-negotiable SLA thresholds (e.g., maximum store replenishment time or minimum safety stock) that cost optimization must not violate?
- Are lease/facility costs fixed long-term, or can capacity reductions immediately translate into OPEX savings?

---

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
### Importance & Business Benefits
- **Single Source of Truth:** Eliminates manual CSV exports and reconciliation discrepancies between operational warehouse stock movements and the ERP/General Ledger (SAP, Oracle, NetSuite).
- **Real-Time Margin & Variance Visibility:** Enables store and logistics managers to see cost anomalies (e.g., sudden freight spikes or stock shrinkage) immediately rather than waiting for month-end closing.
- **Automated Auditability:** Provides an immutable audit trail linking every financial journal entry back to a specific `businessUnitCode`, `Store`, and `Product` transaction.

### Architectural Approach for Seamless Synchronization
1. **Transactional Outbox / Post-Commit Domain Events:**
   - Building on the post-commit pattern (`TransactionPhase.AFTER_SUCCESS` / Outbox table with CDC/Kafka), emit domain events (`WarehouseCreated`, `WarehouseReplaced`, `StockUpdated`) only after local database transactions commit.
2. **Idempotent Consumers & Correlation IDs:**
   - Assign deterministic idempotency keys (e.g., `businessUnitCode + eventTimestamp + transactionId`) so retries never double-book costs in the financial system.
3. **Dead-Letter Queues (DLQ) & Automated Reconciliation:**
   - Route failed sync messages to a DLQ with alerting and run a nightly reconciliation job comparing operational totals against ERP ledger balances.

### Discovery Questions for Stakeholders
- What are the target financial ERP systems, and do they support event-driven webhooks/REST APIs, or do they require batch/EDI interfaces?
- What is the required Recovery Time Objective (RTO) if the downstream financial system is temporarily unavailable during maintenance windows?

---

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
### Importance in Fulfillment Operations
- Fulfillment networks face strong seasonality (holiday peaks, promotional campaigns) alongside hard physical constraints (`Location.maxCapacity`, `Location.maxNumberOfWarehouses`, and `Warehouse.capacity`).
- Accurate forecasting prevents two costly extremes: **over-provisioning** warehouse capacity (paying for idle space/labor) and **under-provisioning** (causing store stockouts and expensive emergency expedited shipping).

### Key System Design Considerations
1. **Historical Time-Series Preservation:**
   - Retain complete historical records across active and archived warehouses (enabled by our soft-archive `archivedAt` design) to train seasonal demand and cost models.
2. **Driver-Based Scenario Modeling ("What-If" Simulation):**
   - Allow planners to simulate scenarios such as: *"If store demand in Amsterdam grows by 30%, when will `AMSTERDAM-001` hit its `maxCapacity` of 100, and what is the cost impact of opening an additional warehouse vs. replacing an existing one?"*
3. **Variance Tracking (Budget vs. Actuals):**
   - Continuously compare forecasted cost per `businessUnitCode` and `Location` against actual incurred costs, triggering alerts when variance exceeds a defined threshold (e.g., >10%).

### Discovery Questions for Stakeholders
- What is the planning horizon (rolling 12-month forecast, quarterly re-forecasting, or weekly operational labor planning)?
- Which external signals (planned store openings, marketing promotions, supplier lead-time changes) need to be ingested into the forecasting model?

---

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
### Why Preserving Cost History Under the Same Business Unit Code Matters
1. **Continuity of Business Unit Benchmarking:**
   - Because the replacement warehouse inherits the `businessUnitCode` (e.g., `MWH.001`) and exact `stock` of the archived warehouse (`archivedAt != null`), financial and operational reports can evaluate the **longitudinal performance of the business unit** across facility transitions while still distinguishing the old facility record (`id = 1`, `createdAt` to `archivedAt`) from the new facility record (`id = 4`, `createdAt` onward).
2. **Baseline for New Warehouse Budget & ROI Validation:**
   - A warehouse replacement typically involves capital expenditure (CAPEX) and transition costs (moving inventory, parallel lease overlap) justified by projected operational savings (higher capacity efficiency, lower unit handling cost). Preserving the old warehouse's historical cost per unit provides the exact baseline needed to verify whether the new warehouse is staying within budget and achieving its expected payback period.
3. **Transition Cost Isolation:**
   - Separating the archived warehouse lifecycle from the new active warehouse record allows finance teams to isolate one-time migration/decommissioning expenses from ongoing run-rate operational budgets.

### Discovery Questions for Stakeholders
- During the replacement transition, is there an overlap period where both facilities incur lease/labor costs simultaneously, and how should those dual-running costs be amortized?
- Should historical dashboards aggregate costs solely by `businessUnitCode` by default, with a drill-down toggle to compare pre-replacement (`archivedAt != null`) vs. post-replacement (`archivedAt == null`) eras?