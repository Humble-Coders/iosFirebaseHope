package org.example.iosfirebasehope


import androidx.compose.material.*
import androidx.compose.runtime.*
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.UI.AddCylinderScreenUI
import org.example.iosfirebasehope.UI.AllCustomersScreenUI
import org.example.iosfirebasehope.UI.AllCylinderDetailsScreenUI
import org.example.iosfirebasehope.UI.AllVendorsScreenUI
import org.example.iosfirebasehope.UI.BillScreenUI
import org.example.iosfirebasehope.UI.CurrentCylinderDetailsUI
import org.example.iosfirebasehope.UI.CustomerDetailsScreenUI
import org.example.iosfirebasehope.UI.CylinderStatusScreenUI
import org.example.iosfirebasehope.UI.ExchangeCylinderScreenUI
import org.example.iosfirebasehope.UI.GasVolumeScreenUI
import org.example.iosfirebasehope.UI.GenerateBillScreenUI
import org.example.iosfirebasehope.UI.GenerateChallanScreenUI
import org.example.iosfirebasehope.UI.HomeScreenUI
import org.example.iosfirebasehope.UI.InventoryScreenUI
import org.example.iosfirebasehope.UI.NewIssueCylinderScreenUI
import org.example.iosfirebasehope.UI.NewOrChooseCustomerScreenUI
import org.example.iosfirebasehope.UI.NewOrChooseVendorScreenUI
import org.example.iosfirebasehope.UI.NotificationScreenUI
import org.example.iosfirebasehope.UI.ReceiveCylinderScreenUI
import org.example.iosfirebasehope.UI.ReturnCylinderScreenUI
import org.example.iosfirebasehope.UI.SendForRefillingScreenUI
import org.example.iosfirebasehope.UI.TransactionDetailsScreen
import org.example.iosfirebasehope.UI.TransactionScreenUI
import org.example.iosfirebasehope.UI.TransactionVendorDetailsScreen
import org.example.iosfirebasehope.UI.TransactionVendorScreenUI
import org.example.iosfirebasehope.UI.VendorDetailsScreenUI
import org.example.iosfirebasehope.UI.VolumeTypeScreenUI
import org.example.iosfirebasehope.navigation.Child
import org.example.iosfirebasehope.navigation.Child.AddCylinderScreen
import org.example.iosfirebasehope.navigation.Child.AllCustomerScreen
import org.example.iosfirebasehope.navigation.Child.AllCylinderDetailsScreen
import org.example.iosfirebasehope.navigation.Child.BillScreen
import org.example.iosfirebasehope.navigation.Child.CurrentCylinderDetailsScreen
import org.example.iosfirebasehope.navigation.Child.CustomerDetailsScreen
import org.example.iosfirebasehope.navigation.Child.CylinderStatusScreen
import org.example.iosfirebasehope.navigation.Child.GasVolumeScreen
import org.example.iosfirebasehope.navigation.Child.HomeScreen
import org.example.iosfirebasehope.navigation.Child.InventoryScreen
import org.example.iosfirebasehope.navigation.Child.NewIssueCylinderScreen
import org.example.iosfirebasehope.navigation.Child.NewOrChooseCustomerScreen
import org.example.iosfirebasehope.navigation.Child.VolumeTypeScreen
import org.example.iosfirebasehope.navigation.RootComponent




@Composable
fun App(rootComponent: RootComponent, db: FirebaseFirestore){
    MaterialTheme{

        val childStack by rootComponent.childStack.subscribeAsState()
        Children(
            stack = childStack,
            animation = stackAnimation(slide())
        ){
                child ->
            when(val instance = child.instance){

                is HomeScreen -> HomeScreenUI(instance.component,db)

                is GasVolumeScreen -> GasVolumeScreenUI(
                    component =  instance.component,
                    gasId = instance.component.gasId, db = db,
                    cylinderDetailList = instance.component.cylinderDetailList
                )

                is AddCylinderScreen -> AddCylinderScreenUI(instance.component, db,instance.component.cylinderDetailList)

                is CylinderStatusScreen -> CylinderStatusScreenUI(instance.component,
                    cylinderDetailsList = instance.component.cylinderDetailsList,
                    status = instance.component.status, gasList = instance.component.gasList)

                is VolumeTypeScreen -> VolumeTypeScreenUI(instance.component, cylinderDetailList = instance.component.cylinderDetailList,VolumeType=instance.component.volumeType, gasId = instance.component.gasId)

                is AllCylinderDetailsScreen -> AllCylinderDetailsScreenUI(instance.component, db= db)
                is CurrentCylinderDetailsScreen -> CurrentCylinderDetailsUI(component = instance.component, initialCylinderDetails = instance.component.currentCylinderDetails, db = db)
                is BillScreen -> BillScreenUI(instance.component, db)
               // is IssueNewCylinderScreen -> IssueNewCylinderScreenUI(instance.component, db)
                is NewOrChooseCustomerScreen -> NewOrChooseCustomerScreenUI(instance.component, db)
                is InventoryScreen -> InventoryScreenUI(instance.component, db)
                is AllCustomerScreen -> AllCustomersScreenUI(instance.component, db, instance.component.cylinderDetailsList, instance.component.gasList)
                is CustomerDetailsScreen -> CustomerDetailsScreenUI(instance.component.customerDetails,instance.component, instance.component.cylinderDetailsList,db, instance.component.gasList)
                is NewIssueCylinderScreen -> NewIssueCylinderScreenUI(instance.component, instance.component.customerName, db)
                is Child.TransactionScreen -> TransactionScreenUI(instance.component.customerName, instance.component, db)
                is Child.NotificationScreen -> NotificationScreenUI(instance.component,db)
                is Child.ReturnCylinderScreen -> ReturnCylinderScreenUI(instance.component.customerName, db, instance.component)
                is Child.ExchangeCylinderScreen -> ExchangeCylinderScreenUI(instance.component, instance.component.customerName, db)
                is Child.NewOrChooseVendorScreen-> NewOrChooseVendorScreenUI(instance.component, db)
                is Child.SendForRefillingScreen-> SendForRefillingScreenUI(
                    instance.component,
                    db = db,
                    VendorName = instance.component.VendorName,
                )
                is Child.ReceiveCylinderScreen -> ReceiveCylinderScreenUI(
                    component =  instance.component,
                    db = db,
                    VendorName = instance.component.VendorName,
                )
                is Child.GenerateBillScreen -> GenerateBillScreenUI(
                    customerName = instance.component.customerName,
                    dateTime = instance.component.dateTime,
                    db = db,
                    instance.component
                )
                is Child.TransactionDetailsScreen -> TransactionDetailsScreen(
                    customerName = instance.component.customerName,
                    dateTime = instance.component.dateTime,
                    db = db,
                    component = instance.component
                )
                is Child.AllVendorScreen -> AllVendorsScreenUI(instance.component, db, instance.component.cylinderDetailsList, instance.component.gasList)
                is Child.VendorDetailsScreen-> VendorDetailsScreenUI(instance.component.vendorDetails,instance.component, instance.component.cylinderDetailsList,db, instance.component.gasList)
                is Child.TransactionVendorScreen-> TransactionVendorScreenUI(instance.component.vendorName, instance.component, db)
                is Child.TransactionVendorDetailsScreen -> TransactionVendorDetailsScreen(instance.component.vendorName, instance.component.dateTime, instance.component, db)
                is Child.GenerateChallanScreen -> GenerateChallanScreenUI(
                    instance.component.VendorName, instance.component.dateTime, db,
                    component = instance.component
                )
            }
        }
    }
}