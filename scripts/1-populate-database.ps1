# PowerShell script za ubacivanje 500+ test transakcija
# Za testiranje složenih upita

$baseUrl = "http://localhost:9050/api/tx"
$headers = @{"Content-Type" = "application/json"}

# Predefined UUIDs za testiranje
$categories = @(
    "550e8400-e29b-41d4-a716-446655440001",  # Elektronika
    "550e8400-e29b-41d4-a716-446655440002",  # Odeća
    "550e8400-e29b-41d4-a716-446655440003",  # Hrana
    "550e8400-e29b-41d4-a716-446655440004",  # Transport
    "550e8400-e29b-41d4-a716-446655440005"   # Zabava
)

$merchants = @(
    "660e8400-e29b-41d4-a716-446655440001",  # TechStore
    "660e8400-e29b-41d4-a716-446655440002",  # FashionHub
    "660e8400-e29b-41d4-a716-446655440003",  # SuperMarket
    "660e8400-e29b-41d4-a716-446655440004",  # BusCompany
    "660e8400-e29b-41d4-a716-446655440005",  # CinemaCity
    "660e8400-e29b-41d4-a716-446655440006",  # BookStore
    "660e8400-e29b-41d4-a716-446655440007",  # PharmacyPlus
    "660e8400-e29b-41d4-a716-446655440008",  # GasStation
    "660e8400-e29b-41d4-a716-446655440009",  # RestaurantX
    "660e8400-e29b-41d4-a716-446655440010"   # GymFit
)

$users = @(
    "770e8400-e29b-41d4-a716-446655440001",  # Ana Marić
    "770e8400-e29b-41d4-a716-446655440002",  # Petar Jovanović
    "770e8400-e29b-41d4-a716-446655440003",  # Milica Nikolić
    "770e8400-e29b-41d4-a716-446655440004",  # Stefan Stojanović
    "770e8400-e29b-41d4-a716-446655440005",  # Jelena Mitrović
    "770e8400-e29b-41d4-a716-446655440006",  # Marko Popović
    "770e8400-e29b-41d4-a716-446655440007",  # Tijana Ilić
    "770e8400-e29b-41d4-a716-446655440008",  # Nemanja Đorđević
    "770e8400-e29b-41d4-a716-446655440009",  # Aleksandra Pavlović
    "770e8400-e29b-41d4-a716-446655440010"   # Milan Radovanović
)

$cards = @(
    "880e8400-e29b-41d4-a716-446655440001",
    "880e8400-e29b-41d4-a716-446655440002",
    "880e8400-e29b-41d4-a716-446655440003",
    "880e8400-e29b-41d4-a716-446655440004",
    "880e8400-e29b-41d4-a716-446655440005",
    "880e8400-e29b-41d4-a716-446655440006",
    "880e8400-e29b-41d4-a716-446655440007",
    "880e8400-e29b-41d4-a716-446655440008",
    "880e8400-e29b-41d4-a716-446655440009",
    "880e8400-e29b-41d4-a716-446655440010"
)

$statuses = @("SUCCESS", "FAILED", "PENDING")

function Get-RandomElement($array) {
    return $array[(Get-Random -Maximum $array.Length)]
}

function Generate-UUID {
    return [System.Guid]::NewGuid().ToString()
}

function Get-RandomAmount {
    # Amounts between 500 and 50000 cents (5-500 RSD)
    return Get-Random -Minimum 500 -Maximum 50000
}

function Get-RandomDate {
    # Random dates in last 60 days
    $days = Get-Random -Minimum 0 -Maximum 60
    $randomDate = (Get-Date).AddDays(-$days)
    return $randomDate.ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
}

Write-Host "🚀🚀🚀 Pokretanje ubacivanja 500+ transakcija..." -ForegroundColor Green

$successCount = 0
$errorCount = 0

for ($i = 1; $i -le 550; $i++) {
    $txId = Generate-UUID
    $userId = Get-RandomElement $users
    $cardId = Get-RandomElement $cards
    $merchantId = Get-RandomElement $merchants
    $categoryId = Get-RandomElement $categories
    $amountCents = Get-RandomAmount
    $status = Get-RandomElement $statuses
    $occurredAt = Get-RandomDate
    
    $transaction = @{
        txId = $txId
        userId = $userId
        cardId = $cardId
        merchantId = $merchantId
        categoryId = $categoryId
        amountCents = $amountCents
        currency = "RSD"
        status = $status
        occurredAt = $occurredAt
    }
    
    $jsonBody = $transaction | ConvertTo-Json -Compress
    
    try {
        $response = Invoke-WebRequest -Uri $baseUrl -Method POST -Headers $headers -Body $jsonBody -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $successCount++
            if ($i % 50 -eq 0) {
                Write-Host "✅ Ubačeno $i transakcija..." -ForegroundColor Green
            }
        }
    }
    catch {
        $errorCount++
        Write-Host "❌ Greška za transakciju $i : $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "" 
Write-Host "🎉🎉🎉 ZAVRŠENO!" -ForegroundColor Green
Write-Host "✅ Uspešno ubačeno: $successCount transakcija" -ForegroundColor Green
Write-Host "❌ Greške: $errorCount" -ForegroundColor Red
Write-Host ""
Write-Host "📊 Kategorije u bazi:" -ForegroundColor Yellow
Write-Host "   - Elektronika: 550e8400-e29b-41d4-a716-446655440001" -ForegroundColor Cyan
Write-Host "   - Odeća:       550e8400-e29b-41d4-a716-446655440002" -ForegroundColor Cyan
Write-Host "   - Hrana:       550e8400-e29b-41d4-a716-446655440003" -ForegroundColor Cyan
Write-Host "   - Transport:   550e8400-e29b-41d4-a716-446655440004" -ForegroundColor Cyan
Write-Host "   - Zabava:      550e8400-e29b-41d4-a716-446655440005" -ForegroundColor Cyan
Write-Host ""
Write-Host "🔍 Sada možete testirati složene upite!" -ForegroundColor Yellow