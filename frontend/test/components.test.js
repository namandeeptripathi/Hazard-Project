/**
 * Stage 8A — Component Unit Tests
 */
import assert from "node:assert";
import { Header } from "../js/components/Header.js";
import { Navigation } from "../js/components/Navigation.js";
import { PageHeader } from "../js/components/PageHeader.js";
import { Card } from "../js/components/Card.js";
import { StatCard } from "../js/components/StatCard.js";
import { StatusBadge } from "../js/components/StatusBadge.js";
import { Button } from "../js/components/Button.js";
import { Tabs } from "../js/components/Tabs.js";
import { LoadingState } from "../js/components/LoadingState.js";
import { EmptyState } from "../js/components/EmptyState.js";
import { ErrorState } from "../js/components/ErrorState.js";
import { SectionHeader } from "../js/components/SectionHeader.js";

export async function runComponentTests() {
    console.log("▶ Running Component Tests...");

    // 1. Header component
    const headerHtml = Header.render({
        platformTitle: "NATIONAL DISASTER DECISION PLATFORM",
        platformSubtitle: "NDRF & SDMA Relocation Intelligence",
        operationalMode: "ACTIVE MONITORING"
    });
    assert.ok(headerHtml.includes("NATIONAL DISASTER DECISION PLATFORM"), "Header contains title");
    assert.ok(headerHtml.includes("ACTIVE MONITORING"), "Header contains operational mode");

    // 2. Navigation component
    const navHtml = Navigation.render({ currentPath: "/overview", currentRegion: "Sitamarhi, Bihar" });
    assert.ok(navHtml.includes('id="nav-overview"'), "Navigation contains overview link");
    assert.ok(navHtml.includes('id="nav-map"'), "Navigation contains map link");
    assert.ok(navHtml.includes('id="nav-safe-sites"'), "Navigation contains safe-sites link");
    assert.ok(navHtml.includes('id="nav-relocation"'), "Navigation contains relocation link");
    assert.ok(navHtml.includes("Sitamarhi, Bihar"), "Navigation contains active region");

    // 3. PageHeader component
    const pageHeaderHtml = PageHeader.render({
        title: "Relocation Decision Intelligence",
        subtitle: "Automated evacuation planning",
        breadcrumbs: [{ label: "Home", path: "#/overview" }, { label: "Relocation" }]
    });
    assert.ok(pageHeaderHtml.includes("Relocation Decision Intelligence"), "PageHeader contains title");
    assert.ok(pageHeaderHtml.includes("Automated evacuation planning"), "PageHeader contains subtitle");

    // 4. Card component
    const cardHtml = Card.render({
        title: "Test Card Title",
        bodyHtml: "<p>Card Body Content</p>",
        footerHtml: "<span>Footer Note</span>",
        elevated: true
    });
    assert.ok(cardHtml.includes("Test Card Title"), "Card contains title");
    assert.ok(cardHtml.includes("card-elevated"), "Card has elevated class");
    assert.ok(cardHtml.includes("Card Body Content"), "Card has body markup");

    // 5. StatCard component
    const statHtml = StatCard.render({
        label: "VULNERABLE POPULATION",
        value: "94,293",
        status: "critical",
        subtitle: "In Flood Zone"
    });
    assert.ok(statHtml.includes("stat-critical"), "StatCard has critical status class");
    assert.ok(statHtml.includes("94,293"), "StatCard displays value");

    // 6. StatusBadge component mappings
    const badgeImmediate = StatusBadge.render({ status: "IMMEDIATE" });
    assert.ok(badgeImmediate.includes("badge-critical"), "IMMEDIATE maps to badge-critical");

    const badgeShortTerm = StatusBadge.render({ status: "SHORT_TERM" });
    assert.ok(badgeShortTerm.includes("badge-warning"), "SHORT_TERM maps to badge-warning");

    const badgeMediumTerm = StatusBadge.render({ status: "MEDIUM_TERM" });
    assert.ok(badgeMediumTerm.includes("badge-moderate"), "MEDIUM_TERM maps to badge-moderate");

    const badgeRecommended = StatusBadge.render({ status: "RECOMMENDED" });
    assert.ok(badgeRecommended.includes("badge-safe"), "RECOMMENDED maps to badge-safe");

    const badgeNoFeasible = StatusBadge.render({ status: "NO_FEASIBLE_DESTINATION" });
    assert.ok(badgeNoFeasible.includes("badge-critical"), "NO_FEASIBLE_DESTINATION maps to badge-critical");

    // 7. Button component
    const btnHtml = Button.render({ label: "Export Plan", variant: "primary", size: "sm" });
    assert.ok(btnHtml.includes("btn-primary"), "Button has primary class");
    assert.ok(btnHtml.includes("btn-sm"), "Button has sm size class");
    assert.ok(btnHtml.includes("Export Plan"), "Button displays label");

    // 8. Tabs component
    const tabsHtml = Tabs.render({
        id: "test-tabs",
        tabs: [
            { id: "tab1", label: "Overview", active: true },
            { id: "tab2", label: "Details", badge: "New" }
        ]
    });
    assert.ok(tabsHtml.includes("test-tabs-tab-tab1"), "Tabs contains tab1 button");
    assert.ok(tabsHtml.includes('aria-selected="true"'), "Active tab has aria-selected true");

    // 9. State components
    const loadingHtml = LoadingState.render({ message: "Fetching decision data..." });
    assert.ok(loadingHtml.includes("loading-spinner"), "LoadingState contains spinner");
    assert.ok(loadingHtml.includes("Fetching decision data..."), "LoadingState displays message");

    const emptyHtml = EmptyState.render({ title: "No Habitats Found", description: "No records match criteria." });
    assert.ok(emptyHtml.includes("No Habitats Found"), "EmptyState displays title");

    const errorHtml = ErrorState.render({ title: "Service Error", message: "Timeout communicating with backend" });
    assert.ok(errorHtml.includes("error-state"), "ErrorState contains error class");
    assert.ok(errorHtml.includes("Timeout communicating with backend"), "ErrorState displays message");

    // 10. SectionHeader component
    const sectionHtml = SectionHeader.render({ title: "Active Decisions", subtitle: "Real-time updates" });
    assert.ok(sectionHtml.includes("Active Decisions"), "SectionHeader displays title");

    console.log("  ✅ All Component Tests Passed.");
}
