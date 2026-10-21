import "./HostelStructureManager.css";

import { useCallback, useEffect, useMemo, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import {
  Layers3,
  Plus,
  RefreshCw,
  Search,
  ChevronRight,
  Building2,
  DoorOpen,
  BedDouble,
  LayoutGrid,
  CheckCircle2,
  AlertCircle,
  X,
  Pencil,
  Trash2,
} from "lucide-react";

import hostelAdminService from "../../services/hostelAdminService";
import axiosClient, { getApiError } from "../../api/axiosClient";

const ROOM_TYPES = [
  { value: "SINGLE", label: "Single" },
  { value: "DOUBLE", label: "Double Sharing" },
  { value: "TRIPLE", label: "Triple Sharing" },
  { value: "DORMITORY", label: "Dormitory" },
  { value: "OTHER", label: "Other" },
];

const PRICING_TYPES = [
  { value: "NORMAL", label: "Normal" },
  { value: "AC", label: "AC" },
];

const EMPTY_FORMS = {
  block: { name: "", code: "", description: "" },
  floor: { name: "", floorNumber: "", description: "" },
  flat: { name: "", code: "", description: "" },
  room: {
    roomNumber: "",
    name: "",
    roomType: "SINGLE",
    capacity: "1",
    description: "",
  },
  bed: {
    bedNumber: "",
    name: "",
    pricingType: "NORMAL",
    price: "",
    description: "",
  },
};

const LEVELS = [
  { id: "blocks", label: "Blocks", icon: Building2 },
  { id: "floors", label: "Floors", icon: Layers3 },
  { id: "flats", label: "Flats (optional)", icon: LayoutGrid },
  { id: "rooms", label: "Rooms", icon: DoorOpen },
  { id: "beds", label: "Beds", icon: BedDouble },
];

const LEVEL_TO_FORM = {
  blocks: "block",
  floors: "floor",
  flats: "flat",
  rooms: "room",
  beds: "bed",
};

function getMessage(error) {
  return (
    getApiError(error) ||
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    "Something went wrong. Please try again."
  );
}

function getId(item) {
  return (
    item?.id ??
    item?.blockId ??
    item?.floorId ??
    item?.flatId ??
    item?.roomId ??
    item?.bedId
  );
}

function normalizeList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.items)) return data.items;
  if (Array.isArray(data?.data)) return data.data;
  return [];
}

function isActive(item) {
  return String(item?.status ?? "ACTIVE").toUpperCase() === "ACTIVE";
}

function isBedAvailable(item) {
  return String(item?.status ?? "AVAILABLE").toUpperCase() === "AVAILABLE";
}

function Status({ item, type }) {
  const status = String(
    item?.status ?? (type === "bed" ? "AVAILABLE" : "ACTIVE")
  ).toUpperCase();

  const active =
    type === "bed" ? status === "AVAILABLE" : status === "ACTIVE";

  return (
    <span className={`hsm-status ${active ? "active" : "inactive"}`}>
      <span />
      {status}
    </span>
  );
}

export default function HostelStructureManager({ hostelId }) {
  const [blocks, setBlocks] = useState([]);
  const [floors, setFloors] = useState([]);
  const [flats, setFlats] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [beds, setBeds] = useState([]);

  const [blockId, setBlockId] = useState("");
  const [floorId, setFloorId] = useState("");
  const [flatId, setFlatId] = useState("");
  const [roomId, setRoomId] = useState("");
  const [roomParent, setRoomParent] = useState("floor");

  const [level, setLevel] = useState("blocks");
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const [formLevel, setFormLevel] = useState("");
  const [form, setForm] = useState({ ...EMPTY_FORMS.block });
  const [editingItem, setEditingItem] = useState(null);

  const selectedBlock = blocks.find(
    (item) => String(getId(item)) === String(blockId)
  );

  const selectedFloor = floors.find(
    (item) => String(getId(item)) === String(floorId)
  );

  const selectedFlat = flats.find(
    (item) => String(getId(item)) === String(flatId)
  );

  const selectedRoom = rooms.find(
    (item) => String(getId(item)) === String(roomId)
  );

  // --------------------------------------------------
  // Load hierarchy
  // --------------------------------------------------

  const loadBlocks = useCallback(async () => {
    if (!hostelId) {
      setBlocks([]);
      setBlockId("");
      setFloors([]);
      setFlats([]);
      setRooms([]);
      setBeds([]);
      return;
    }

    const list = normalizeList(
      await hostelAdminService.getBlocks(hostelId)
    );

    setBlocks(list);

    setBlockId((current) => {
      if (list.some((item) => String(getId(item)) === String(current))) {
        return current;
      }

      return String(getId(list[0]) ?? "");
    });
  }, [hostelId]);

  const loadFloors = useCallback(async () => {
    if (!hostelId || !blockId) {
      setFloors([]);
      setFloorId("");
      return;
    }

    const list = normalizeList(
      await hostelAdminService.getFloors(hostelId, blockId)
    );

    setFloors(list);

    setFloorId((current) => {
      if (list.some((item) => String(getId(item)) === String(current))) {
        return current;
      }

      return String(getId(list[0]) ?? "");
    });
  }, [hostelId, blockId]);

  const loadFlats = useCallback(async () => {
    if (!hostelId || !blockId || !floorId) {
      setFlats([]);
      setFlatId("");
      return;
    }

    const list = normalizeList(
      await hostelAdminService.getFlats(hostelId, blockId, floorId)
    );

    setFlats(list);

    setFlatId((current) => {
      if (list.some((item) => String(getId(item)) === String(current))) {
        return current;
      }

      return String(getId(list[0]) ?? "");
    });
  }, [hostelId, blockId, floorId]);

  const loadRooms = useCallback(async () => {
    if (!hostelId || !blockId || !floorId) {
      setRooms([]);
      setRoomId("");
      return;
    }

    let data;

    if (roomParent === "flat") {
      if (!flatId) {
        setRooms([]);
        setRoomId("");
        return;
      }

      data = await hostelAdminService.getFlatRooms(
        hostelId,
        blockId,
        floorId,
        flatId
      );
    } else {
      data = await hostelAdminService.getRooms(
        hostelId,
        blockId,
        floorId
      );
    }

    const list = normalizeList(data);
    setRooms(list);

    setRoomId((current) => {
      if (list.some((item) => String(getId(item)) === String(current))) {
        return current;
      }

      return String(getId(list[0]) ?? "");
    });
  }, [hostelId, blockId, floorId, flatId, roomParent]);

  const loadBeds = useCallback(async () => {
    if (!hostelId || !blockId || !floorId || !roomId) {
      setBeds([]);
      return;
    }

    let data;

    if (roomParent === "flat") {
      if (!flatId) {
        setBeds([]);
        return;
      }

      data = await hostelAdminService.getFlatRoomBeds(
        hostelId,
        blockId,
        floorId,
        flatId,
        roomId
      );
    } else {
      data = await hostelAdminService.getBeds(
        hostelId,
        blockId,
        floorId,
        roomId
      );
    }

    setBeds(normalizeList(data));
  }, [hostelId, blockId, floorId, flatId, roomId, roomParent]);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      await loadBlocks();
    } catch (err) {
      setError(`Could not load blocks: ${getMessage(err)}`);
    } finally {
      setLoading(false);
    }
  }, [loadBlocks]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  useEffect(() => {
    loadFloors().catch((err) =>
      setError(`Could not load floors: ${getMessage(err)}`)
    );
  }, [loadFloors]);

  useEffect(() => {
    loadFlats().catch((err) =>
      setError(`Could not load flats: ${getMessage(err)}`)
    );
  }, [loadFlats]);

  useEffect(() => {
    loadRooms().catch((err) =>
      setError(`Could not load rooms: ${getMessage(err)}`)
    );
  }, [loadRooms]);

  useEffect(() => {
    loadBeds().catch((err) =>
      setError(`Could not load beds: ${getMessage(err)}`)
    );
  }, [loadBeds]);

  // --------------------------------------------------
  // Search and counts
  // --------------------------------------------------

  const rows = useMemo(() => {
    const source = {
      blocks,
      floors,
      flats,
      rooms,
      beds,
    }[level] || [];

    const query = search.trim().toLowerCase();

    if (!query) return source;

    return source.filter((item) =>
      Object.values(item || {}).some(
        (value) =>
          value != null && String(value).toLowerCase().includes(query)
      )
    );
  }, [blocks, floors, flats, rooms, beds, level, search]);

  const currentCount = {
    blocks: blocks.length,
    floors: floors.length,
    flats: flats.length,
    rooms: rooms.length,
    beds: beds.length,
  };

  const levelReady = {
    blocks: Boolean(hostelId),
    floors: Boolean(hostelId && blockId),
    flats: Boolean(hostelId && blockId && floorId),
    rooms:
      Boolean(hostelId && blockId && floorId) &&
      (roomParent === "floor" || Boolean(flatId)),
    beds: Boolean(hostelId && blockId && floorId && roomId),
  }[level];

  const typeForLevel = LEVEL_TO_FORM[level];

  // --------------------------------------------------
  // Form handling: create and edit
  // --------------------------------------------------

  function openCreateForm(nextLevel) {
    setError("");
    setNotice("");
    setEditingItem(null);
    setFormLevel(nextLevel);
    setForm({ ...EMPTY_FORMS[nextLevel] });
  }

  function openEditForm(item, nextLevel) {
    setError("");
    setNotice("");
    setEditingItem(item);
    setFormLevel(nextLevel);

    const defaults = EMPTY_FORMS[nextLevel];

    setForm({
      ...defaults,
      ...Object.fromEntries(
        Object.keys(defaults).map((key) => [
          key,
          item[key] == null ? defaults[key] : String(item[key]),
        ])
      ),
      roomType: item.roomType || defaults.roomType,
      pricingType: item.pricingType || defaults.pricingType,
    });
  }

  function closeForm() {
    if (saving) return;
    setFormLevel("");
    setEditingItem(null);
  }

  function updateField(key, value) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function saveForm(event) {
    event.preventDefault();

    if (!formLevel || saving) return;

    setSaving(true);
    setError("");
    setNotice("");

    try {
      const clean = Object.fromEntries(
        Object.entries(form).map(([key, value]) => [
          key,
          typeof value === "string" ? value.trim() : value,
        ])
      );

      const id = editingItem ? getId(editingItem) : null;
      const editing = Boolean(editingItem);

      if (formLevel === "block") {
        if (editing) {
          await hostelAdminService.updateBlock(hostelId, id, clean);
        } else {
          await hostelAdminService.createBlock(hostelId, clean);
        }
        await loadBlocks();
        setLevel("blocks");
      }

      if (formLevel === "floor") {
        const payload = {
          ...clean,
          floorNumber: Number(clean.floorNumber),
        };

        if (editing) {
          await hostelAdminService.updateFloor(
            hostelId,
            blockId,
            id,
            payload
          );
        } else {
          await hostelAdminService.createFloor(
            hostelId,
            blockId,
            payload
          );
        }

        await loadFloors();
        setLevel("floors");
      }

      if (formLevel === "flat") {
        if (editing) {
          await hostelAdminService.updateFlat(
            hostelId,
            blockId,
            floorId,
            id,
            clean
          );
        } else {
          await hostelAdminService.createFlat(
            hostelId,
            blockId,
            floorId,
            clean
          );
        }

        await loadFlats();
        setLevel("flats");
      }

      if (formLevel === "room") {
        const payload = {
          ...clean,
          capacity: Number(clean.capacity),
        };

        if (
          !ROOM_TYPES.some((roomType) => roomType.value === payload.roomType)
        ) {
          throw new Error("Please select a valid room type.");
        }

        if (roomParent === "flat") {
          if (editing) {
            await hostelAdminService.updateFlatRoom(
              hostelId,
              blockId,
              floorId,
              flatId,
              id,
              payload
            );
          } else {
            await hostelAdminService.createFlatRoom(
              hostelId,
              blockId,
              floorId,
              flatId,
              payload
            );
          }
        } else if (editing) {
          await hostelAdminService.updateRoom(
            hostelId,
            blockId,
            floorId,
            id,
            payload
          );
        } else {
          await hostelAdminService.createRoom(
            hostelId,
            blockId,
            floorId,
            payload
          );
        }

        await loadRooms();
        setLevel("rooms");
      }

      if (formLevel === "bed") {
        const payload = {
          ...clean,
          price: clean.price === "" ? null : Number(clean.price),
        };

        if (roomParent === "flat") {
          if (editing) {
            await hostelAdminService.updateFlatRoomBed(
              hostelId,
              blockId,
              floorId,
              flatId,
              roomId,
              id,
              payload
            );
          } else {
            await hostelAdminService.createFlatRoomBed(
              hostelId,
              blockId,
              floorId,
              flatId,
              roomId,
              payload
            );
          }
        } else if (editing) {
          await hostelAdminService.updateBed(
            hostelId,
            blockId,
            floorId,
            roomId,
            id,
            payload
          );
        } else {
          await hostelAdminService.createBed(
            hostelId,
            blockId,
            floorId,
            roomId,
            payload
          );
        }

        await loadBeds();
        setLevel("beds");
      }

      setNotice(
        `${formLevel.charAt(0).toUpperCase() + formLevel.slice(1)} ${
          editing ? "updated" : "created"
        } successfully.`
      );

      setFormLevel("");
      setEditingItem(null);
    } catch (err) {
      setError(getMessage(err));
    } finally {
      setSaving(false);
    }
  }

  // --------------------------------------------------
  // Status changes
  // --------------------------------------------------

  async function toggleStatus(item, type) {
    const id = getId(item);

    if (id == null) {
      setError("The selected record does not contain an ID.");
      return;
    }

    const nextStatus =
      type === "bed"
        ? isBedAvailable(item)
          ? "MAINTENANCE"
          : "AVAILABLE"
        : isActive(item)
          ? "INACTIVE"
          : "ACTIVE";

    if (!window.confirm(`Change status to ${nextStatus}?`)) return;

    setError("");
    setNotice("");

    try {
      if (type === "block") {
        await hostelAdminService.changeBlockStatus(hostelId, id, nextStatus);
        await loadBlocks();
      } else if (type === "floor") {
        await hostelAdminService.changeFloorStatus(
          hostelId,
          blockId,
          id,
          nextStatus
        );
        await loadFloors();
      } else if (type === "flat") {
        await hostelAdminService.changeFlatStatus(
          hostelId,
          blockId,
          floorId,
          id,
          nextStatus
        );
        await loadFlats();
      } else if (type === "room") {
        if (roomParent === "flat") {
          await hostelAdminService.changeFlatRoomStatus(
            hostelId,
            blockId,
            floorId,
            flatId,
            id,
            nextStatus
          );
        } else {
          await hostelAdminService.changeRoomStatus(
            hostelId,
            blockId,
            floorId,
            id,
            nextStatus
          );
        }

        await loadRooms();
      } else if (type === "bed") {
        if (roomParent === "flat") {
          await hostelAdminService.changeFlatRoomBedStatus(
            hostelId,
            blockId,
            floorId,
            flatId,
            roomId,
            id,
            nextStatus
          );
        } else {
          await hostelAdminService.changeBedStatus(
            hostelId,
            blockId,
            floorId,
            roomId,
            id,
            nextStatus
          );
        }

        await loadBeds();
      }

      setNotice(`Status changed to ${nextStatus.toLowerCase()}.`);
    } catch (err) {
      setError(getMessage(err));
    }
  }

  // --------------------------------------------------
  // Delete
  // --------------------------------------------------

  async function deleteItem(item, type) {
    const id = getId(item);

    if (id == null) {
      setError("The selected record does not contain an ID.");
      return;
    }

    if (!window.confirm(`Delete this ${type}? This cannot be undone.`)) {
      return;
    }

    setError("");
    setNotice("");

    try {
      let url;

      if (type === "block") {
        url = `/api/hostels/${hostelId}/blocks/${id}`;
      } else if (type === "floor") {
        url = `/api/hostels/${hostelId}/blocks/${blockId}/floors/${id}`;
      } else if (type === "flat") {
        url = `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${id}`;
      } else if (type === "room" && roomParent === "flat") {
        url = `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${id}`;
      } else if (type === "room") {
        url = `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${id}`;
      } else if (type === "bed" && roomParent === "flat") {
        url = `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/flats/${flatId}/rooms/${roomId}/beds/${id}`;
      } else {
        url = `/api/hostels/${hostelId}/blocks/${blockId}/floors/${floorId}/rooms/${roomId}/beds/${id}`;
      }

      await axiosClient.delete(url);

      if (type === "block") await loadBlocks();
      if (type === "floor") await loadFloors();
      if (type === "flat") await loadFlats();
      if (type === "room") await loadRooms();
      if (type === "bed") await loadBeds();

      setNotice(`${type.charAt(0).toUpperCase() + type.slice(1)} deleted.`);
    } catch (err) {
      setError(getMessage(err));
    }
  }

  // --------------------------------------------------
  // Form fields
  // --------------------------------------------------

  const fields = {
    block: [
      ["name", "Block name", "e.g. North Block", "text", true],
      ["code", "Block code", "e.g. NB-01", "text", true],
      ["description", "Description", "Optional details", "textarea", false],
    ],
    floor: [
      ["name", "Floor name", "e.g. First Floor", "text", true],
      ["floorNumber", "Floor number", "e.g. 1", "number", true],
      ["description", "Description", "Optional details", "textarea", false],
    ],
    flat: [
      ["name", "Flat name", "e.g. Flat A", "text", true],
      ["code", "Flat code", "e.g. A-101", "text", true],
      ["description", "Description", "Optional details", "textarea", false],
    ],
    room: [
      ["roomNumber", "Room number", "e.g. 101", "text", true],
      ["name", "Room name", "e.g. Study Room", "text", false],
      ["roomType", "Room type", "", "select", true],
      ["capacity", "Capacity (beds)", "e.g. 4", "number", true],
      ["description", "Description", "Optional details", "textarea", false],
    ],
    bed: [
      ["bedNumber", "Bed number", "e.g. B-01", "text", true],
      ["name", "Bed name", "e.g. Window bed", "text", false],
      ["pricingType", "Pricing type", "", "pricing", true],
      ["price", "Price", "Enter amount", "number", true],
      ["description", "Description", "Optional details", "textarea", false],
    ],
  };

  return (
    <section className="hsm">
      <div className="hsm-heading">
        <div>
          <span className="hsm-eyebrow">HOSTEL LAYOUT</span>
          <h2>Structure management</h2>
          <p>Manage blocks, floors, flats, rooms and beds.</p>
        </div>

        <button
          className="hsm-button secondary"
          type="button"
          onClick={refresh}
          disabled={loading}
        >
          <RefreshCw
            size={16}
            className={loading ? "hsm-spin" : ""}
          />
          Refresh
        </button>
      </div>

      {/* Hierarchy selectors */}
      <div className="hsm-hierarchy">
        <div className="hsm-select">
          <label htmlFor="hsm-block">Block</label>
          <select
            id="hsm-block"
            value={blockId}
            onChange={(event) => {
              setBlockId(event.target.value);
              setFloorId("");
              setFlatId("");
              setRoomId("");
              setError("");
            }}
          >
            <option value="">Select block</option>
            {blocks.map((item) => (
              <option key={getId(item)} value={getId(item)}>
                {item.name || item.code || `Block ${getId(item)}`}
              </option>
            ))}
          </select>
        </div>

        <ChevronRight size={17} />

        <div className="hsm-select">
          <label htmlFor="hsm-floor">Floor</label>
          <select
            id="hsm-floor"
            value={floorId}
            disabled={!blockId}
            onChange={(event) => {
              setFloorId(event.target.value);
              setFlatId("");
              setRoomId("");
              setError("");
            }}
          >
            <option value="">Select floor</option>
            {floors.map((item) => (
              <option key={getId(item)} value={getId(item)}>
                {item.name || `Floor ${item.floorNumber}`}
              </option>
            ))}
          </select>
        </div>

        <ChevronRight size={17} />

        <div className="hsm-select">
          <label htmlFor="hsm-flat">Flat (optional)</label>
          <select
            id="hsm-flat"
            value={flatId}
            disabled={!floorId}
            onChange={(event) => {
              setFlatId(event.target.value);
              setRoomId("");
              setRoomParent(event.target.value ? "flat" : "floor");
              setError("");
            }}
          >
            <option value="">No flat / direct rooms</option>
            {flats.map((item) => (
              <option key={getId(item)} value={getId(item)}>
                {item.name || item.code || `Flat ${getId(item)}`}
              </option>
            ))}
          </select>
        </div>

        <ChevronRight size={17} />

        <div className="hsm-select">
          <label htmlFor="hsm-room">Room</label>
          <select
            id="hsm-room"
            value={roomId}
            disabled={!floorId}
            onChange={(event) => setRoomId(event.target.value)}
          >
            <option value="">Select room</option>
            {rooms.map((item) => (
              <option key={getId(item)} value={getId(item)}>
                {item.roomNumber || item.name || `Room ${getId(item)}`}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Level tabs */}
      <div className="hsm-level-tabs">
        {LEVELS.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            type="button"
            className={level === id ? "selected" : ""}
            onClick={() => {
              setLevel(id);
              setSearch("");
              setError("");
            }}
          >
            <Icon size={17} />
            {label}
            <span>{currentCount[id]}</span>
          </button>
        ))}
      </div>

      {/* Toolbar */}
      <div className="hsm-toolbar">
        <div>
          <h3>{LEVELS.find((item) => item.id === level)?.label}</h3>
          <p>
            {level === "blocks" && "Buildings or residential blocks."}
            {level === "floors" &&
              (selectedBlock
                ? `Floors in ${selectedBlock.name || selectedBlock.code}.`
                : "Select a block first.")}
            {level === "flats" &&
              (selectedFloor
                ? `Flats on ${selectedFloor.name || "the selected floor"}.`
                : "Select a floor first.")}
            {level === "rooms" &&
              (roomParent === "flat" && selectedFlat
                ? `Rooms inside ${selectedFlat.name || selectedFlat.code}.`
                : "Rooms directly on the selected floor.")}
            {level === "beds" &&
              (selectedRoom
                ? `Beds in room ${
                    selectedRoom.roomNumber ||
                    selectedRoom.name ||
                    getId(selectedRoom)
                  }.`
                : "Select a room above.")}
          </p>
        </div>

        <button
          type="button"
          className="hsm-button primary"
          disabled={!levelReady}
          onClick={() => openCreateForm(typeForLevel)}
        >
          <Plus size={17} />
          Add {typeForLevel}
        </button>
      </div>

      {/* Room parent selection */}
      {level === "rooms" && (
        <div className="hsm-room-mode">
          <label>
            <input
              type="radio"
              name="roomParent"
              checked={roomParent === "floor"}
              onChange={() => {
                setRoomParent("floor");
                setRoomId("");
              }}
            />
            Rooms directly on floor
          </label>

          <label>
            <input
              type="radio"
              name="roomParent"
              checked={roomParent === "flat"}
              disabled={!flatId}
              onChange={() => {
                setRoomParent("flat");
                setRoomId("");
              }}
            />
            Rooms inside selected flat
          </label>
        </div>
      )}

      {/* Search */}
      <div className="hsm-search">
        <Search size={17} />
        <input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder={`Search ${level}…`}
          aria-label={`Search ${level}`}
        />

        {search && (
          <button
            type="button"
            onClick={() => setSearch("")}
            aria-label="Clear search"
          >
            <X size={15} />
          </button>
        )}
      </div>

      {/* Alerts */}
      {error && (
        <div className="hsm-alert error" role="alert">
          <AlertCircle size={17} />
          <span>{error}</span>
          <button
            type="button"
            onClick={() => setError("")}
            aria-label="Dismiss error"
          >
            <X size={15} />
          </button>
        </div>
      )}

      {notice && (
        <div className="hsm-alert success" role="status">
          <CheckCircle2 size={17} />
          <span>{notice}</span>
          <button
            type="button"
            onClick={() => setNotice("")}
            aria-label="Dismiss notification"
          >
            <X size={15} />
          </button>
        </div>
      )}

      {/* Records table */}
      {loading ? (
        <div className="hsm-empty">
          <RefreshCw className="hsm-spin" size={22} />
          Loading structure…
        </div>
      ) : !levelReady ? (
        <div className="hsm-empty">
          <Layers3 size={28} />
          <h3>Select the parent first</h3>
          <p>Select the required block, floor, flat or room above.</p>
        </div>
      ) : rows.length === 0 ? (
        <div className="hsm-empty">
          <Layers3 size={28} />
          <h3>{search ? "No matching records" : `No ${level} created yet`}</h3>
          <p>
            {search
              ? "Try a different search."
              : `Use Add ${typeForLevel} to create the first one.`}
          </p>
        </div>
      ) : (
        <div className="hsm-table-wrap">
          <table className="hsm-table">
            <thead>
              <tr>
                <th>Name / Number</th>
                <th>Details / Pricing</th>
                <th>Status</th>
                <th>Description</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              <AnimatePresence initial={false}>
                {rows.map((item) => {
                  const type = typeForLevel;

                  const name =
                    item.name ||
                    item.roomNumber ||
                    item.bedNumber ||
                    (item.floorNumber != null
                      ? `Floor ${item.floorNumber}`
                      : `Record ${getId(item)}`);

                  const details = [
                    item.code,
                    item.roomType,
                    item.capacity != null ? `Capacity: ${item.capacity}` : null,
                    item.pricingType,
                    item.price != null ? `Price: ${item.price}` : null,
                  ]
                    .filter(Boolean)
                    .join(" · ");

                  return (
                    <motion.tr
                      key={getId(item)}
                      initial={{ opacity: 0, y: 5 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0 }}
                    >
                      <td>
                        <strong>{name}</strong>
                      </td>
                      <td>{details || "—"}</td>
                      <td>
                        <Status item={item} type={type} />
                      </td>
                      <td>{item.description || "—"}</td>
                      <td>
                        <div className="hsm-actions">
                          <button
                            type="button"
                            className="hsm-button secondary small"
                            onClick={() => openEditForm(item, type)}
                            title={`Edit ${type}`}
                          >
                            <Pencil size={14} />
                            Edit
                          </button>

                          <button
                            type="button"
                            className="hsm-button secondary small"
                            onClick={() => toggleStatus(item, type)}
                          >
                            {type === "bed"
                              ? isBedAvailable(item)
                                ? "Mark maintenance"
                                : "Mark available"
                              : isActive(item)
                                ? "Deactivate"
                                : "Activate"}
                          </button>

                          <button
                            type="button"
                            className="hsm-button secondary small"
                            onClick={() => deleteItem(item, type)}
                            title={`Delete ${type}`}
                          >
                            <Trash2 size={14} />
                            Delete
                          </button>
                        </div>
                      </td>
                    </motion.tr>
                  );
                })}
              </AnimatePresence>
            </tbody>
          </table>
        </div>
      )}

      {/* Create / edit modal */}
      <AnimatePresence>
        {formLevel && (
          <div
            className="hsm-backdrop"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget) closeForm();
            }}
          >
            <motion.section
              className="hsm-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="hsm-modal-title"
              initial={{ opacity: 0, y: 12, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 8 }}
            >
              <header>
                <div className="hsm-modal-icon">
                  {editingItem ? <Pencil size={20} /> : <Plus size={20} />}
                </div>

                <div>
                  <h3 id="hsm-modal-title">
                    {editingItem ? "Edit" : "Add"} {formLevel}
                  </h3>
                  <p>Fill in the details below.</p>
                </div>

                <button
                  type="button"
                  className="hsm-close"
                  onClick={closeForm}
                  disabled={saving}
                  aria-label="Close"
                >
                  <X size={18} />
                </button>
              </header>

              <form onSubmit={saveForm}>
                {fields[formLevel].map(
                  ([key, label, placeholder, type, required]) => (
                    <label className="hsm-field" key={key}>
                      {label}
                      {required && <span className="required"> *</span>}

                      {type === "textarea" ? (
                        <textarea
                          rows={3}
                          maxLength={1000}
                          required={required}
                          value={form[key] ?? ""}
                          onChange={(event) =>
                            updateField(key, event.target.value)
                          }
                          placeholder={placeholder}
                        />
                      ) : type === "select" && key === "roomType" ? (
                        <select
                          value={form.roomType}
                          required={required}
                          onChange={(event) =>
                            updateField("roomType", event.target.value)
                          }
                        >
                          {ROOM_TYPES.map((roomType) => (
                            <option
                              key={roomType.value}
                              value={roomType.value}
                            >
                              {roomType.label}
                            </option>
                          ))}
                        </select>
                      ) : type === "pricing" ? (
                        <select
                          value={form.pricingType}
                          required={required}
                          onChange={(event) =>
                            updateField("pricingType", event.target.value)
                          }
                        >
                          {PRICING_TYPES.map((pricingType) => (
                            <option
                              key={pricingType.value}
                              value={pricingType.value}
                            >
                              {pricingType.label}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <input
                          type={type}
                          min={type === "number" ? 0 : undefined}
                          step={type === "number" ? "any" : undefined}
                          required={required}
                          value={form[key] ?? ""}
                          onChange={(event) =>
                            updateField(key, event.target.value)
                          }
                          placeholder={placeholder}
                        />
                      )}
                    </label>
                  )
                )}

                {formLevel === "room" && (
                  <p className="hsm-form-hint">
                    Capacity is the number of beds in this room.
                  </p>
                )}

                {formLevel === "bed" && (
                  <p className="hsm-form-hint">
                    Pricing type and price are sent with the bed payload.
                    Ensure the backend Bed DTO accepts these fields.
                  </p>
                )}

                <div className="hsm-modal-actions">
                  <button
                    type="button"
                    className="hsm-button secondary"
                    onClick={closeForm}
                    disabled={saving}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className="hsm-button primary"
                    disabled={saving}
                  >
                    {saving
                      ? "Saving…"
                      : `${editingItem ? "Update" : "Create"} ${formLevel}`}
                  </button>
                </div>
              </form>
            </motion.section>
          </div>
        )}
      </AnimatePresence>
    </section>
  );
}