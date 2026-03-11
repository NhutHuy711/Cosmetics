var trackRecordCount;

$(document).ready(function() {
	trackRecordCount = $(".hiddenTrackId").length;

    $("#trackList").on('blur', "textarea[name='trackNotes']", function () {
        const noteField = $(this);

        // Skip validate nếu status RETURNED (notes sẽ auto-set khi Save)
        const st = noteField.closest("tr").find("select[name='trackStatus']").val();
        if (st === "RETURNED") return;

        const maxLength = 256;

        if (!noteField.val().toString().trim()) {
            showWarningMessage(messages.NOT_NULL_NOTES);
            noteField.addClass('is-invalid');
            noteField.focus();
        } else if (noteField.val().toString().length > maxLength) {
            showWarningMessage(messages.EXCEED_MAX_LENGTH_NOTES);
            noteField.addClass('is-invalid');
            noteField.focus();
        } else {
            noteField.removeClass('is-invalid');
        }
    });

    $("#trackList").on("click", ".linkRemoveTrack", function(e) {
		e.preventDefault();
		deleteTrack($(this));
		updateTrackCountNumbers();
		updateOverviewStatus();
	});

	$("#track").on("click", "#linkAddTrack", function(e) {
		e.preventDefault();
		addNewTrackRecord();
		updateTrackStatusOptions();
	});

	$("#trackList").on("change", ".dropDownStatus", function(e) {
		const dropdown = $(this);
		updateTrackStatusOptions();
		updateTrackNotesBasedOnStatus(dropdown); // Gọi hàm cập nhật notes khi status thay đổi
	});

	$("#trackList").on("change", "input[name='trackDate'], select[name='trackStatus']", function() {
		updateOverviewStatus();
	});

    $("#trackList").on("change", ".chkRestock", function() {
        const rowNumber = $(this).data("row");
        const checked = $(this).is(":checked");
        $(`#hiddenRestock${rowNumber}`).val(checked ? "true" : "false");
    });

    updateTrackStatusOptions();

    $(".dropDownStatus").each(function() {
        const $dd = $(this);
        const rowNumber = $dd.attr("rowNumber");
        renderReturnedUI(rowNumber, $dd.val());

        // Chỉ init checkbox/hidden nếu row có checkbox
        if ($(`#returnedOptions${rowNumber}`).length) {
            $(`#hiddenRestock${rowNumber}`).val("false");
            $(`#restock${rowNumber}`).prop("checked", false);
        }
    });

    let isSubmitting = false;

    $("#submitButton").on("click", function(e) {
        if (isSubmitting) return;   // tránh submit vòng lặp
        e.preventDefault();

        // 1) Chuẩn hoá notes cho các dòng RETURNED trước
        let needRestock = false;

        $(".dropDownStatus").each(function() {
            const $dd = $(this);
            const rowNumber = $dd.attr("rowNumber");
            const st = $dd.val();

            if (st === "RETURNED" && isNewTrackRow(rowNumber)) {
                const restock = ($(`#hiddenRestock${rowNumber}`).val() === "true");

                $(`#trackNote${rowNumber}`).val(
                    restock
                        ? "Order has been returned and store the product in the warehouse"
                        : "Order has been returned"
                );

                // show notes để user thấy ngay trước khi submit
                $(`#trackNote${rowNumber}`).show();
                $(`#returnedOptions${rowNumber}`).hide();

                needRestock = needRestock || restock;
            }
        });

        // 2) Nếu không restock: submit form luôn (vẫn chạy processFormBeforeSubmit)
        if (!needRestock) {
            isSubmitting = true;
            $("#orderForm").trigger("submit");
            return;
        }

        // 3) Nếu có restock: gọi API trước rồi submit
        const orderId = $("#id").val(); // th:field="*{id}" sẽ sinh id="id"

        $.ajax({
            type: "POST",
            url: contextPath + "api/orders/" + orderId + "/restock",
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeaderName, csrfValue);
            },
            success: function() {
                isSubmitting = true;
                $("#orderForm").trigger("submit");
            },
            error: function(xhr) {
                console.error(xhr);
                showWarningMessage("Restock failed. Please try again.");
            }
        });
    });

});

const statusDescriptions = {
	NEW: "Order was placed by the customer",
	CANCELLED: "Order was rejected",
	PROCESSING: "Order is being processed",
	PACKAGED: "Products were packaged",
	PICKED: "Shipper picked the package",
	SHIPPING: "Shipper is delivering the package",
	DELIVERED: "Customer received products",
	RETURN_REQUESTED: "Customer sent request to return purchase",
	RETURNED: "Products were returned",
	PAID: "Customer has paid this order",
	REFUNDED: "Customer has been refunded"
};

function updateTrackNotesBasedOnStatus(dropdown) {
    const selectedStatus = dropdown.val();
    const rowNumber = dropdown.attr("rowNumber");

    renderReturnedUI(rowNumber, selectedStatus);

    if (selectedStatus === "RETURNED") return;

    const defaultDescription = statusDescriptions[selectedStatus];
    if (defaultDescription) {
        $(`#trackNote${rowNumber}`).val(defaultDescription);
    }
}


function updateTrackStatusOptions() {
	let selectedStatuses = [];

	// Lấy tất cả các giá trị đã chọn từ các dropdown
	$(".dropDownStatus").each(function() {
		const selectedValue = $(this).val();
		if (selectedValue) {
			selectedStatuses.push(selectedValue);
		}
	});

	// Cập nhật danh sách dropdown để loại bỏ các giá trị đã chọn
	$(".dropDownStatus").each(function() {
		const currentDropdown = $(this);
		const currentValue = currentDropdown.val();

		// Xóa tất cả các tùy chọn
		currentDropdown.find("option").each(function() {
			$(this).show(); // Hiển thị tất cả tùy chọn trước khi lọc
			if ($(this).val() !== currentValue && selectedStatuses.includes($(this).val())) {
				$(this).hide(); // Ẩn các tùy chọn đã được chọn
			}
		});
	});
}

function updateOverviewStatus() {
	let latestDate = null;
	let latestStatus = null;

	console.log("Updating overview status:");

	let rows = $("#trackList tbody tr").toArray();
	rows.sort((rowA, rowB) => {
		let dateA = new Date($(rowA).find("input[name='trackDate']").val());
		let dateB = new Date($(rowB).find("input[name='trackDate']").val());
		return dateB - dateA; // Sắp xếp theo ngày giảm dần
	});

	rows.forEach(row => {
		let dateInput = $(row).find("input[name='trackDate']").val();
		let currentDate = new Date(dateInput);
		let currentStatus = $(row).find("select[name='trackStatus']").val();

		console.log("Row data:", { trackDate: dateInput, parsedDate: currentDate, status: currentStatus });

		if (!currentDate || isNaN(currentDate)) {
			console.warn("Invalid date:", dateInput);
			return;
		}

		if (!latestDate || currentDate > latestDate) {
			latestDate = currentDate;
			latestStatus = currentStatus;
		}
	});

	console.log("Latest status:", { latestDate, latestStatus });

	if (latestStatus) {
		$("#overviewStatus").val(latestStatus);
	} else {
		$("#overviewStatus").val("NEW");
	}
}



function deleteTrack(link) {
	var rowNumber = link.attr('rowNumber');
	$("#rowTrack" + rowNumber).remove();
	$("#emptyLine" + rowNumber).remove();
}

function updateTrackCountNumbers() {
	$(".divCountTrack").each(function (index, element) {
		element.innerHTML = "" + (index + 1);
	});
}

function addNewTrackRecord() {
    const htmlCode = generateTrackRowCode();
    $("#trackList tbody").append(htmlCode);

    // trackRecordCount lúc này chính là "nextCount" vừa tạo
    const newRowNumber = trackRecordCount;

    $(`#hiddenRestock${newRowNumber}`).val("false");

    const newDropdown = $(`#rowTrack${newRowNumber} select[name='trackStatus']`);
    updateTrackNotesBasedOnStatus(newDropdown);
}

function generateTrackRowCode() {
	var nextCount = trackRecordCount + 1;
	trackRecordCount++;
	var rowId = "rowTrack" + nextCount;
	var trackNoteId = "trackNote" + nextCount;
	var currentDateTime = formatCurrentDateTime();

	htmlCode = `
		<tr id="${rowId}">
			<input type="hidden" name="trackId" value="0" class="hiddenTrackId" />
			<td>
				<input type="datetime-local" name="trackDate" value="${currentDateTime}" class="form-control" required style="width: 100%;" readonly/>
			</td>
			<td>
				<select name="trackStatus" class="form-control dropDownStatus" required style="max-width: 300px" rowNumber="${nextCount}">
					<option value="CANCELLED" defaultDescription="Order has been cancelled">CANCELLED</option>
					<option value="PROCESSING" defaultDescription="Order is being processed">PROCESSING</option>
					<option value="PACKAGED" defaultDescription="Order has been packaged">PACKAGED</option>
					<option value="PICKED" defaultDescription="Order has been picked">PICKED</option>
					<option value="SHIPPING" defaultDescription="Order is in shipping">SHIPPING</option>
					<option value="DELIVERED" defaultDescription="Order has been delivered">DELIVERED</option>
					<option value="RETURN_REQUESTED" defaultDescription="Return has been requested">RETURN_REQUESTED</option>
					<option value="RETURNED" defaultDescription="Order has been returned">RETURNED</option>
					<option value="REFUNDED" defaultDescription="Order has been refunded">REFUNDED</option>
				</select>
			</td>
			<td>
                  <textarea rows="2" class="form-control trackNotes" name="trackNotes"
                            id="${trackNoteId}" style="width: 100%;"></textarea>
                
                  <div class="returned-options mt-2" id="returnedOptions${nextCount}" style="display:none">
                    <div class="form-check">
                      <input class="form-check-input chkRestock" type="checkbox"
                             id="restock${nextCount}" data-row="${nextCount}">
                      <label class="form-check-label" for="restock${nextCount}">
                        Store the product in the warehouse.
                      </label>
                    </div>
                
                    <input type="hidden" name="restockToStock"
                           class="hiddenRestock" id="hiddenRestock${nextCount}" value="false">
                  </div>
                </td>
			<td>
				<a class="text-danger linkRemoveTrack" href="" rowNumber="${nextCount}">
					<i class="feather-icon icon-trash-2"></i>
				</a>
			</td>
		</tr>
	`;

	return htmlCode;
}

function isNewTrackRow(rowNumber) {
    const $row = $(`#rowTrack${rowNumber}`);
    const trackId = $row.find("input.hiddenTrackId[name='trackId']").val();
    return trackId === "0" || trackId === 0;
}


function formatCurrentDateTime() {
	var date = new Date();
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	var hour = date.getHours();
	var minute = date.getMinutes();
	var second = date.getSeconds();

	if (month < 10) month = "0" + month;
	if (day < 10) day = "0" + day;
	if (hour < 10) hour = "0" + hour;
	if (minute < 10) minute = "0" + minute;
	if (second < 10) second = "0" + second;

	return year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second;
}

const returnedNotes = {
    restocked: "Products were returned and restocked",
    notRestocked: "Products were returned and not restocked"
};

function renderReturnedUI(rowNumber, statusValue) {
    const $textarea = $(`#trackNote${rowNumber}`);
    const $optBox = $(`#returnedOptions${rowNumber}`);

    // Nếu row không có checkbox (do th:if) thì $optBox.length = 0
    if (statusValue === "RETURNED" && isNewTrackRow(rowNumber) && $optBox.length) {
        // row mới: ẩn notes, hiện checkbox
        $textarea.hide();
        $optBox.show();
    } else {
        // row cũ hoặc status khác: notes hiển thị bình thường
        if ($optBox.length) $optBox.hide();
        $textarea.show();
    }
}

function applyReturnedNotes(rowNumber) {
    const restock = ($(`#hiddenRestock${rowNumber}`).val() === "true");
    $(`#trackNote${rowNumber}`).val(restock ? returnedNotes.warehouse : returnedNotes.base);
}


