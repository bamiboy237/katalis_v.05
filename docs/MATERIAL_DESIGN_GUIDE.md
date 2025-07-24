# Material Design 3 Guide for Educational Apps (Jetpack Compose)

This guide provides a comprehensive reference for implementing Material Design 3 in Android educational applications using Jetpack Compose. It covers color, typography, icons, components, layout, motion, and accessibility, with a focus on developer-friendly implementation details and expressive design principles.

---

## 1. Color System Reference

Material 3's color system is based on a set of tonal palettes derived from key colors. These roles are automatically assigned for light and dark themes, ensuring a harmonious and accessible UI.

### Color Roles

| Role | Description |
| --- | --- |
| **Primary** | The base color for main components like buttons and active states. |
| **On Primary** | Color for content (text, icons) on top of the Primary color. |
| **Secondary** | Used for less prominent components that need to stand out. |
| **On Secondary** | Color for content on top of the Secondary color. |
| **Tertiary** | Used for contrasting accents that can balance primary and secondary colors. |
| **On Tertiary** | Color for content on top of the Tertiary color. |
| **Error** | Indicates errors in components, like invalid text fields. |
| **On Error** | Color for content on top of the Error color. |
| **Surface** | The primary background color for components like cards, sheets, and menus. |
| **On Surface** | The primary color for text and icons on top of the Surface color. |
| **Surface Variant**| A subtly different surface color for differentiation. |
| **On Surface Variant**| Color for text and icons on top of the Surface Variant color. |
| **Outline** | Used for component borders and dividers. |

### Expressive Color

Go beyond the default color roles to create a more vibrant and engaging experience.

*   **Expanded Color Palette:** Use a wider range of colors to create better hierarchy and clarity.
*   **Nuanced Color Application:** Apply color to decorative elements and backgrounds to guide user attention.
*   **Personalization:** Allow users to customize the color scheme to their preferences.

### Light & Dark Theme Mappings

The `material3-theme-builder` can generate a complete `ColorScheme` for you. In Jetpack Compose, these are accessed via `MaterialTheme.colorScheme`.

```kotlin
// Accessing colors in Compose
val primaryColor = MaterialTheme.colorScheme.primary
val surfaceColor = MaterialTheme.colorScheme.surface
```

### Semantic Colors for Educational Apps

Use semantic colors consistently to provide intuitive feedback.

| State | Color Role | Use Case |
| --- | --- | --- |
| **Success** | `tertiary` (or custom) | Correct quiz answers, completed lessons, positive reinforcement. |
| **Warning** | `secondary` (or custom)| Gentle alerts, upcoming deadlines, hints. |
| **Error** | `error` | Incorrect quiz answers, failed submissions, critical alerts. |

**Example: Quiz Answer Feedback**

```kotlin
val feedbackColor = when (answerState) {
    AnswerState.CORRECT -> MaterialTheme.colorScheme.tertiaryContainer
    AnswerState.INCORRECT -> MaterialTheme.colorScheme.errorContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
}
```

---

## 2. Typography Scale

Material 3 provides a comprehensive type scale with 15 styles. Using these consistently creates a clear visual hierarchy.

### Font Family Recommendations

*   **Serif (e.g., Noto Serif):** Excellent for headings and titles, conveying a classic, academic feel.
*   **Sans-serif (e.g., Roboto, Noto Sans):** Ideal for body text and UI elements due to its high legibility on screens.

### Expressive Typography

*   **Visually Emphasized Typography:** Use new type styles to create a clearer visual hierarchy and draw attention to important actions.
*   **Weight and Style Variation:** Use a variety of font weights and styles to create a more dynamic and engaging reading experience.

### Type Scale & Specifications

| Style | Font Size (sp) | Line Height (sp) | Letter Spacing (sp) | Usage in Educational Apps |
| --- | --- | --- | --- | --- |
| **Display Large** | 57 | 64 | -0.25 | Major screen titles (e.g., "Biology 101") |
| **Display Medium** | 45 | 52 | 0 | |
| **Display Small** | 36 | 44 | 0 | |
| **Headline Large** | 32 | 40 | 0 | Lesson titles, major section heads |
| **Headline Medium** | 28 | 36 | 0 | |
| **Headline Small** | 24 | 32 | 0 | Sub-section heads |
| **Title Large** | 22 | 28 | 0 | Card titles, dialog titles |
| **Title Medium** | 16 | 24 | 0.15 | List item titles |
| **Title Small** | 14 | 20 | 0.1 | |
| **Label Large** | 14 | 20 | 0.1 | Buttons |
| **Label Medium** | 12 | 16 | 0.5 | Small icon labels |
| **Label Small** | 11 | 16 | 0.5 | Overline text |
| **Body Large** | 16 | 24 | 0.5 | Main lesson content, paragraphs |
| **Body Medium** | 14 | 20 | 0.25 | Secondary text, captions |
| **Body Small** | 12 | 16 | 0.4 | Tertiary text, metadata |

### Hierarchy for Lesson Content

```kotlin
Column(modifier = Modifier.padding(16.dp)) {
    Text("Chapter 1: The Cell", style = MaterialTheme.typography.headlineLarge)
    Spacer(Modifier.height(16.dp))
    Text(
        text = "All living organisms are made up of cells...",
        style = MaterialTheme.typography.bodyLarge
    )
}
```

---

## 3. Icon System

Material Symbols are the preferred icon library for Material 3, offering variable axes for weight, fill, and optical size.

### Material Icons vs. Material Symbols

| Feature | Material Icons | Material Symbols |
| --- | --- | --- |
| **Format** | Static font | Variable font |
| **Styling** | 5 styles (Filled, Outlined, Rounded, Two-Tone, Sharp) | 3 variable axes (Weight, Fill, Optical Size) |
| **Best For** | Simplicity, consistency | Expressiveness, flexibility |

### Sizing Standards

*   **`24dp`**: Standard size for most use cases (e.g., app bars, list items).
*   **`18dp`**: For dense layouts or when paired with smaller text.
*   **`48dp`**: For prominent actions or illustrative purposes.

### Icon Color Treatments and States

| State | Color |
| --- | --- |
| **Default** | `MaterialTheme.colorScheme.onSurface` |
| **Active** | `MaterialTheme.colorScheme.primary` |
| **Inactive** | `MaterialTheme.colorScheme.onSurfaceVariant` |
| **Disabled** | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)` |

### Educational-Specific Icons

| Use Case | Material Symbol Name |
| --- | --- |
| Lesson/Course | `school`, `book` |
| Quiz/Test | `quiz`, `checklist` |
| Progress | `trending_up`, `hourglass_top` |
| Navigation | `arrow_forward`, `arrow_back` |
| Science | `science`, `biotech` |

### Icon Usage in Jetpack Compose

1.  **Add the dependency:**

    ```groovy
    implementation "androidx.compose.material:material-icons-extended:<version>"
    ```
2.  **Use the `Icon` composable:**

    ```kotlin
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.School

    Icon(
        imageVector = Icons.Default.School,
        contentDescription = "Lesson", // Essential for accessibility
        tint = MaterialTheme.colorScheme.primary
    )
    ```

### Icon Button Specifications and Touch Targets

Ensure touch targets are at least `48x48dp`.

```kotlin
IconButton(onClick = { /* ... */ }) {
    Icon(
        imageVector = Icons.Default.Quiz,
        contentDescription = "Start Quiz"
    )
}
```

---

## 4. Component Specifications

### Expressive Components

Material 3 Expressive introduces 15 new or updated components with greater configuration, more shape options, and emphasized text.

*   **Expanded Shape Library:** A collection of 35 new shapes can be used for decorative elements, with a built-in morphing animation for smooth transitions.
*   **Shape Variety:** Use a variety of shapes to create a more dynamic and engaging UI.

### Cards: Filled, Elevated, Outlined

*   **Filled:** Default, provides the most emphasis.
*   **Elevated:** Sits on a surface color with a subtle shadow.
*   **Outlined:** For secondary actions; has a border and no shadow.

**Example: Quiz Card**

```kotlin
OutlinedCard(
    modifier = Modifier.fillMaxWidth(),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("What is the powerhouse of the cell?", style = MaterialTheme.typography.titleMedium)
        // ... Add RadioButtons for options
    }
}
```

### Buttons: Filled, Outlined, Text

*   **Filled:** For the most important action on a screen.
*   **Outlined:** For medium-emphasis actions.
*   **Text:** For low-emphasis, supplementary actions.

### Navigation: Bottom Navigation, Top App Bar

*   **Bottom Navigation:** For top-level navigation between 3-5 destinations.
*   **Top App Bar:** Use for screen titles and primary actions.

### Form Elements: Text Fields, Radio Buttons, Checkboxes

Use `TextField`, `RadioButton`, and `Checkbox` with proper state management and labels.

### Progress Indicators: Linear and Circular

*   **Linear:** For determinate or indeterminate progress on a linear scale.
*   **Circular:** For determinate or indeterminate progress in a circular format.

### Educational-Specific Patterns

*   **Quiz Cards:** Use `OutlinedCard` or `ElevatedCard` to display quiz questions and options.
*   **Lesson Progress:** Use a `LinearProgressIndicator` to show the user's progress through a lesson.
*   **Results Display:** Use a combination of `Text`, `Icon`, and `Card` to display quiz results in a clear and engaging way.

### Buttons

*   **Filled:** For the most important action on a screen.
*   **Outlined:** For medium-emphasis actions.
*   **Text:** For low-emphasis, supplementary actions.

```kotlin
Button(onClick = { /* Submit Quiz */ }) {
    Text("Submit")
}
```

### Navigation

*   **Top App Bar:** Use for screen titles and primary actions.
*   **Bottom Navigation:** For top-level navigation between 3-5 destinations.

### Form Elements

Use `TextField`, `RadioButton`, and `Checkbox` with proper state management and labels.

---

## 5. Layout Guidelines

### Spacing System

Use a consistent grid based on multiples of **8dp** for margins, padding, and spacing between elements. Use **4dp** for smaller, component-level spacing.

*   **Screen Margins:** `16dp` on each side.
*   **Content Padding:** `16dp` or `24dp` inside containers like Cards.
*   **Spacers:** `Spacer(Modifier.height(8.dp))`

### Safe Area Considerations

Use `WindowInsets` to respect system bars and display cutouts.

```kotlin
Scaffold(
    modifier = Modifier.fillMaxSize()
) { innerPadding ->
    LazyColumn(
        contentPadding = innerPadding
    ) {
        // ... content
    }
}
```

---

## 6. Motion and Interaction

Motion should be purposeful and provide feedback.

### Expressive Motion

*   **Motion-Physics System:** A new system utilizing motion springs creates more fluid and natural animations.
*   **Spring Animations:** Use spring animations to create a more playful and engaging user experience.

### Durations

*   Short (100-150ms): Fades, micro-interactions.
*   Medium (200-250ms): Small expansions.
*   Long (300ms+): Full-screen transitions.

### Easing

Use `LinearOutSlowInEasing` for elements entering the screen and `FastOutLinearInEasing` for elements exiting.

### Page Transitions

Use `Crossfade` for simple screen changes or `AnimatedContent` for more complex transitions.

**Example: Feedback Animation**

```kotlin
AnimatedVisibility(
    visible = showFeedback,
    enter = fadeIn(animationSpec = tween(300)),
    exit = fadeOut(animationSpec = tween(300))
) {
    // ... Correct/Incorrect feedback message
}
```

---

## 7. Accessibility Standards

Designing for accessibility is crucial for educational apps.

*   **Touch Targets:** All interactive elements (`Button`, `IconButton`, `RadioButton`) must have a minimum size of **`48x48dp`**. Use `Modifier.minimumTouchTargetSize()` if needed.
*   **Color Contrast:**
    *   **4.5:1** for normal text (`Body`, `Title`).
    *   **3:1** for large text (`Headline`, `Display`).
*   **Text Sizing:** Ensure your app respects the user's system font size settings. Use `sp` for all text.
*   **Screen Readers:** Provide a `contentDescription` for all non-textual elements like `Icon` and `Image`.