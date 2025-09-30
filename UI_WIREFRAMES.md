# Product Import UI Wireframes

## 1. Import Wizard - Step 1: File Upload

```
┌─────────────────────────────────────────────────────────────────┐
│ Product Import - Admin Dashboard                                │
├─────────────────────────────────────────────────────────────────┤
│ Step 1: Upload File                                             │
│                                                                 │
│ Select Import Source:                                           │
│ ┌─────────────────┐  ┌─────────────────┐                       │
│ │   Excel File    │  │ Google Sheets   │                       │
│ │  [📊] .xlsx/.xls│  │ [☁️] URL Import  │                       │
│ │                 │  │                 │                       │
│ └─────────────────┘  └─────────────────┘                       │
│                                                                 │
│ File Upload Area:                                               │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │                                                             │ │
│ │        📁 Drop your Excel file here                         │ │
│ │           or click to browse                                │ │
│ │                                                             │ │
│ │              [Choose File]                                  │ │
│ │                                                             │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ [Next: Map Columns]                                             │
└─────────────────────────────────────────────────────────────────┘
```

## 2. Import Wizard - Step 2: Column Mapping

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 2: Map Columns                                             │
├─────────────────────────────────────────────────────────────────┤
│ Sheet Selection: [Sheet1 ▼]                                    │
│                                                                 │
│ Data Preview (First 5 rows):                                   │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ Name        │ Price │ Category │ Stock │ Description        │ │
│ ├─────────────┼───────┼──────────┼───────┼────────────────────┤ │
│ │ Product A   │ 29.99 │ Electronics│ 100 │ High quality...    │ │
│ │ Product B   │ 19.99 │ Clothing  │ 50  │ Comfortable...     │ │
│ │ Product C   │ 39.99 │ Books     │ 25  │ Educational...     │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ Map Excel Columns to Product Fields:                           │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ Name        → [Product Name (Required) ▼]     ✓            │ │
│ │ Price       → [Price (Required) ▼]            ✓            │ │
│ │ Category    → [Category ▼]                    ✓            │ │
│ │ Stock       → [Stock Quantity ▼]              ✓            │ │
│ │ Description → [Description ▼]                 ✓            │ │
│ │ Image       → [Image URL ▼]                   ✓            │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ [Back]                                    [Next: Import]       │
└─────────────────────────────────────────────────────────────────┘
```

## 3. Import Wizard - Step 3: Import Settings

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 3: Import Products                                         │
├─────────────────────────────────────────────────────────────────┤
│ Import Settings:                                                │
│ ☐ Update existing products                                      │
│ ☑ Skip duplicate products                                       │
│                                                                 │
│ Batch Size: [100] rows per batch                               │
│                                                                 │
│ Import Summary:                                                 │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ File: products.xlsx                                         │ │
│ │ Sheet: Sheet1                                               │ │
│ │ Total Rows: 1,250                                           │ │
│ │ Mapped Fields: 6                                            │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ [Back]                                    [Start Import]       │
└─────────────────────────────────────────────────────────────────┘
```

## 4. Import Progress Screen

```
┌─────────────────────────────────────────────────────────────────┐
│ Import Progress                                                 │
├─────────────────────────────────────────────────────────────────┤
│ Progress: 750 / 1,250 (60.0%)                                  │
│ ████████████████████████████████████████████████████████████████ │
│                                                                 │
│ Statistics:                                                     │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                │
│ │   720       │ │    30       │ │   60.0%     │                │
│ │ Successful  │ │   Failed    │ │  Complete   │                │
│ └─────────────┘ └─────────────┘ └─────────────┘                │
│                                                                 │
│ Import Errors:                                                  │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ Row 45: Price - Invalid price format                       │ │
│ │ Row 67: Category - Category 'Invalid' does not exist       │ │
│ │ Row 89: Stock - Stock cannot be negative                   │ │
│ │ Row 123: Name - Required field cannot be empty             │ │
│ │ ...                                                         │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ [View Results]                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 5. Import History Page

```
┌─────────────────────────────────────────────────────────────────┐
│ Import History - Admin Dashboard                                │
├─────────────────────────────────────────────────────────────────┤
│ Statistics:                                                     │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │
│ │     25      │ │     20      │ │      3      │ │      2      │ │
│ │Total Imports│ │Successful   │ │   Failed    │ │In Progress  │ │
│ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ │
│                                                                 │
│ Import History:                                                 │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ File        │ Status    │ Progress │ Results    │ Actions   │ │
│ ├─────────────┼───────────┼──────────┼────────────┼───────────┤ │
│ │products.xlsx│ Completed │ 100%     │ 720/30     │ 👁️ 📥     │ │
│ │inventory.xls│ Processing│ 60%      │ 450/15     │ 👁️ ⏹️     │ │
│ │items.xlsx   │ Failed    │ 100%     │ 0/100      │ 👁️ 📥     │ │
│ │catalog.xlsx │ Completed │ 100%     │ 1,200/0    │ 👁️        │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ [New Import]                                                    │
└─────────────────────────────────────────────────────────────────┘
```

## 6. Error Details Modal

```
┌─────────────────────────────────────────────────────────────────┐
│ Import Details                                                  │
├─────────────────────────────────────────────────────────────────┤
│ File Name: products.xlsx                                        │
│ Status: [Completed]                                             │
│                                                                 │
│ Total Rows: 1,250    Processed Rows: 1,250                     │
│ Successful: 720      Failed: 30                                │
│                                                                 │
│ Errors:                                                         │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ Row 45: Price - Invalid price format                       │ │
│ │   Cell Value: "not a number"                               │ │
│ │                                                             │ │
│ │ Row 67: Category - Category 'Invalid' does not exist       │ │
│ │   Cell Value: "Invalid"                                    │ │
│ │                                                             │ │
│ │ Row 89: Stock - Stock cannot be negative                   │ │
│ │   Cell Value: "-5"                                         │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ [Close]                                                         │
└─────────────────────────────────────────────────────────────────┘
```

## Key UI Features

### 1. Multi-Step Wizard
- Clear progress indication
- Step validation before proceeding
- Back/Next navigation
- Cancel option at any step

### 2. File Upload
- Drag and drop support
- File type validation
- Visual feedback for file selection
- Support for both Excel and Google Sheets

### 3. Column Mapping
- Data preview table
- Dropdown mapping interface
- Required field indicators
- Automatic mapping suggestions

### 4. Progress Tracking
- Real-time progress bar
- Success/failure statistics
- Error list with details
- Cancel running imports

### 5. History Management
- Complete import history
- Status filtering
- Error report downloads
- Detailed import information

### 6. Error Handling
- Categorized error types
- Row and column references
- Detailed error messages
- Cell value display
- Error report generation

## Responsive Design

The UI is built with Tailwind CSS and is fully responsive:
- Mobile-first design approach
- Collapsible sidebar on mobile
- Responsive tables with horizontal scroll
- Touch-friendly interface elements
- Optimized for both desktop and tablet use

## Accessibility Features

- Keyboard navigation support
- Screen reader compatibility
- High contrast color schemes
- Clear visual hierarchy
- Descriptive button labels
- Error message announcements
