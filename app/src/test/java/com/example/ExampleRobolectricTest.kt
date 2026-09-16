package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.SectorType
import com.example.model.SeedData
import com.example.security.TenantSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Spatial7", appName)
  }

  @Test
  fun `verify all four sector tenants exist in seed data`() {
    val tenants = SeedData.TENANTS
    assertEquals(4, tenants.size)
    val sectors = tenants.map { it.sectorType }
    assertTrue(sectors.contains(SectorType.AGRICULTURE))
    assertTrue(sectors.contains(SectorType.LOCAL_GOV))
    assertTrue(sectors.contains(SectorType.EDUCATION))
    assertTrue(sectors.contains(SectorType.INDUSTRIAL))
  }

  @Test
  fun `verify RLS security manager denies cross-tenant query`() {
    val tenantAgri = SeedData.TENANTS[0]
    val tenantCouncil = SeedData.TENANTS[1]

    val audit = TenantSecurityManager.verifyCrossTenantQuery(
      activeTenantId = tenantAgri.tenantId,
      requestedTenantId = tenantCouncil.tenantId,
      sqlQuery = "SELECT * FROM spatial_assets WHERE tenant_id = '${tenantCouncil.tenantId}'"
    )

    assertFalse(audit.isAllowed)
    assertEquals("403 Forbidden - RLS Denial", audit.statusCode)
    assertEquals(0, audit.leakageCount)
  }

  @Test
  fun `verify RLS security manager permits same-tenant query`() {
    val tenantAgri = SeedData.TENANTS[0]

    val audit = TenantSecurityManager.verifyCrossTenantQuery(
      activeTenantId = tenantAgri.tenantId,
      requestedTenantId = tenantAgri.tenantId,
      sqlQuery = "SELECT * FROM spatial_assets WHERE tenant_id = '${tenantAgri.tenantId}'"
    )

    assertTrue(audit.isAllowed)
    assertEquals("200 OK - RLS Passed", audit.statusCode)
    assertEquals(0, audit.leakageCount)
  }
}
